function Sync-HomeScripts {
  $baseRawUrl = "https://raw.githubusercontent.com/brbaro/home/refs/heads/main/Scripts"
  Write-Host "Mi cueva su cueva... 🛠️`n" -ForegroundColor Yellow

  $files = try {
    $list = Invoke-RestMethod -Uri "$baseRawUrl/.brbaro.list" -UseBasicParsing -ErrorAction Stop
    $list -split "`n" | Where-Object { $_.Trim() }
  } catch {
    Write-Host "[!] Error: Could not fetch script list" -ForegroundColor Red
    return
  }

  $destDir = "$HOME\Scripts"
  $localDir = "$destDir\Local"
  $ignoreList = @(".brbaro.list", "Local", "Cache")

  if (!(Test-Path $destDir)) { New-Item -Path $destDir -ItemType Directory -Force | Out-Null }
  if (!(Test-Path $localDir)) { New-Item -Path $localDir -ItemType Directory -Force | Out-Null }

  Write-Host "Cleaning up orphaned scripts..." -ForegroundColor Yellow
  if (Test-Path $destDir) {
    Get-ChildItem -Path $destDir | ForEach-Object {
      if ($files -notcontains $_.Name -and $ignoreList -notcontains $_.Name) {
        Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "  [-] Removed: $($_.Name)" -ForegroundColor DarkRed
      }
    }
  }

  Write-Host "Fetching scripts from '$baseRawUrl'..." -ForegroundColor Cyan
  foreach ($fileName in $files) {
    $fileName = $fileName.Trim()
    $targetPath = Join-Path $destDir $fileName
    $url = "$baseRawUrl/$fileName"
    try {
      $remoteContent = Invoke-RestMethod -Uri $url -UseBasicParsing -ErrorAction Stop

      $finalContent = ($remoteContent -replace "\r", "").Trim() + "`n"
      if ($fileName.EndsWith(".bat")) { $finalContent = $finalContent -replace "\n", "`r`n" }

      $shouldWrite = $true
      if (Test-Path $targetPath) {
        $localContent = Get-Content $targetPath -Raw
        $cleanRemote = $finalContent.Trim() -replace "\r", ""
        $cleanLocal = ($localContent -replace "\r", "").Trim()
        if ($cleanRemote -eq $cleanLocal) { $shouldWrite = $false }
      }

      if ($shouldWrite) {
        Set-Content -Path $targetPath -Value $finalContent -Encoding String
        Write-Host "  > $fileName synced" -ForegroundColor Gray
      } else {
        Write-Host "  . $fileName skipped" -ForegroundColor Gray
      }
    } catch {
      Write-Host "  [!] Failed to download $fileName" -ForegroundColor Red
    }
  }

  Update-UserPath -Paths @($destDir, $localDir)

  if (Test-Path $destDir) { (Get-Item $destDir -Force).Attributes = 'Hidden' }
  Write-Host "`nBárbaro! 🚀" -ForegroundColor Green
}

function Update-UserPath {
  param($Paths)
  $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
  $pathArray = @($currentPath -split ';' | Where-Object { $_ -match '\S' })
  $updateRequired = $false

  foreach ($p in $Paths) {
    $cleanP = $p.TrimEnd('\')
    if ($pathArray -replace '\\$', '' -notcontains $cleanP) {
      $pathArray += $cleanP
      $updateRequired = $true
      Write-Host "[+] Adding $cleanP to User PATH" -ForegroundColor Cyan
    }
  }

  if ($updateRequired) {
    $cleanPathString = ($pathArray | Where-Object { $_ -match '\S' }) -join ';'
    [Environment]::SetEnvironmentVariable("Path", $cleanPathString, "User")
    $env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + $cleanPathString
  }
}

Sync-HomeScripts
