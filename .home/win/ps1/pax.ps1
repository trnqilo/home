param(
  [string]$Action,
  [string]$PackageName
)

function Get-PaxConfiguration {
  $BaseUrl = if ($env:PAX_URL) { $env:PAX_URL.TrimEnd('/') } else { "https://brbaro.web.app/pax" }
  if ($BaseUrl -notmatch "^[a-z]+://") { $BaseUrl = "https://$BaseUrl" }

  return @{
    Url    = $BaseUrl
    Cache  = Join-Path $Home "Scripts\Cache"
    Binary = Join-Path $env:LOCALAPPDATA "Pax"
  }
}

function Normalize-PaxPathEntry {
  param([string]$PathValue)

  if ([string]::IsNullOrWhiteSpace($PathValue)) { return $null }

  $Normalized = $PathValue.Trim().Trim('"')
  while ($Normalized.Length -gt 3 -and ($Normalized.EndsWith('\') -or $Normalized.EndsWith('/'))) {
    $Normalized = $Normalized.Substring(0, $Normalized.Length - 1)
  }

  return $Normalized
}

function Test-PaxPathEntryExists {
  param(
    [string]$PathValue,
    [string]$TargetDir
  )

  $NormalizedTarget = Normalize-PaxPathEntry -PathValue $TargetDir
  if (-not $NormalizedTarget) { return $false }

  foreach ($Segment in ($PathValue -split ';')) {
    $NormalizedSegment = Normalize-PaxPathEntry -PathValue $Segment
    if ($NormalizedSegment -and $NormalizedSegment -eq $NormalizedTarget) {
      return $true
    }
  }

  return $false
}

function Remove-PaxPathEntry {
  param([string]$TargetDir)

  $NormalizedTarget = Normalize-PaxPathEntry -PathValue $TargetDir
  if (-not $NormalizedTarget) { return }

  $CurrentUserPath = [Environment]::GetEnvironmentVariable("Path", "User")
  $FilteredUserEntries = foreach ($Segment in ($CurrentUserPath -split ';')) {
    $NormalizedSegment = Normalize-PaxPathEntry -PathValue $Segment
    if ($NormalizedSegment -and $NormalizedSegment -ne $NormalizedTarget) {
      $NormalizedSegment
    }
  }
  [Environment]::SetEnvironmentVariable("Path", (@($FilteredUserEntries) -join ';'), "User")

  $FilteredSessionEntries = foreach ($Segment in ($env:Path -split ';')) {
    $NormalizedSegment = Normalize-PaxPathEntry -PathValue $Segment
    if ($NormalizedSegment -and $NormalizedSegment -ne $NormalizedTarget) {
      $NormalizedSegment
    }
  }
  $env:Path = @($FilteredSessionEntries) -join ';'
}

function Update-UserEnvironmentPath {
  param([string]$TargetDir)
  $CurrentPath = [Environment]::GetEnvironmentVariable("Path", "User")
  if (-not (Test-PaxPathEntryExists -PathValue $CurrentPath -TargetDir $TargetDir)) {
    $NewPath = if ([string]::IsNullOrWhiteSpace($CurrentPath)) {
      $TargetDir
    } else {
      "$($CurrentPath.TrimEnd(';'));$TargetDir"
    }

    [Environment]::SetEnvironmentVariable("Path", $NewPath, "User")
    if (-not (Test-PaxPathEntryExists -PathValue $env:Path -TargetDir $TargetDir)) {
      $env:Path = if ([string]::IsNullOrWhiteSpace($env:Path)) {
        $TargetDir
      } else {
        "$($env:Path.TrimEnd(';'));$TargetDir"
      }
    }

    Write-Host "[+] PATH Registered: $TargetDir" -ForegroundColor Cyan
  }
}

function Receive-PaxPackage {
  param($Config, $Name, $ZipPath)
  Write-Host "[+] Fetching $Name.pax" -ForegroundColor Gray
  $Payload = Invoke-RestMethod -Uri "$($Config.Url)/$Name.pax" -UseBasicParsing
  $Bytes = [System.Convert]::FromBase64String(($Payload -replace '\s+', ''))
  [System.IO.File]::WriteAllBytes($ZipPath, $Bytes)
}

function Get-PaxPackageItems {
  param([string]$Path)

  return @(Get-ChildItem -Path $Path -Force | Where-Object {
    $_.Name -ne "__MACOSX" -and
    $_.Name -ne ".DS_Store" -and
    -not $_.Name.StartsWith("._")
  })
}

function Resolve-PaxPortablePathTarget {
  param([string]$Destination)

  $DestinationItems = @(Get-PaxPackageItems -Path $Destination)
  $DestinationDirectories = @($DestinationItems | Where-Object { $_.PSIsContainer })
  $DestinationFiles = @($DestinationItems | Where-Object { -not $_.PSIsContainer })

  if ($DestinationFiles.Count -eq 0 -and $DestinationDirectories.Count -eq 1) {
    return $DestinationDirectories[0].FullName
  }

  return $Destination
}

function Deploy-PortableItems {
  param($Items, $Config, $Name)
  $Destination = Join-Path $Config.Binary $Name
  if (Test-Path $Destination) { Remove-Item $Destination -Recurse -Force }
  New-Item $Destination -ItemType Directory -Force | Out-Null

  foreach ($Item in @($Items)) {
    Move-Item -Path $Item.FullName -Destination $Destination -Force
  }

  $PathTarget = Resolve-PaxPortablePathTarget -Destination $Destination
  if ($PathTarget -ne $Destination) {
    Remove-PaxPathEntry -TargetDir $Destination
  }

  Update-UserEnvironmentPath -TargetDir $PathTarget
}

function Start-PaxInstaller {
  param($InstallerPath, $Name)

  Write-Host "[+] Launching $Name installer..." -ForegroundColor Cyan
  if ($InstallerPath.Extension -eq ".msi") {
    Start-Process msiexec.exe -ArgumentList "/i `"$($InstallerPath.FullName)`" /quiet" -Wait
  } else {
    Start-Process $InstallerPath.FullName -ArgumentList "/S" -Wait
  }
}

function Invoke-PaxInstall {
  param($Name)
  $Config = Get-PaxConfiguration
  $PackageCache = Join-Path $Config.Cache $Name
  $ZipPath = Join-Path $PackageCache "$Name.zip"
  $TempExtract = Join-Path $PackageCache "raw_extract"

  if (!(Test-Path $PackageCache)) { New-Item $PackageCache -ItemType Directory -Force | Out-Null }

  Receive-PaxPackage -Config $Config -Name $Name -ZipPath $ZipPath

  if (Test-Path $TempExtract) { Remove-Item $TempExtract -Recurse -Force }
  try {
    Expand-Archive -Path $ZipPath -DestinationPath $TempExtract -Force

    $RootItems = @(Get-PaxPackageItems -Path $TempExtract)
    if ($RootItems.Count -eq 0) {
      throw "Package '$Name' is empty after extraction."
    }

    $RootDirectories = @($RootItems | Where-Object { $_.PSIsContainer })
    $RootFiles = @($RootItems | Where-Object { -not $_.PSIsContainer })
    $RootInstallers = @($RootFiles | Where-Object { $_.Extension -in @(".exe", ".msi") })

    if ($RootInstallers.Count -gt 0) {
      Start-PaxInstaller -InstallerPath ($RootInstallers | Select-Object -First 1) -Name $Name
    }
    elseif ($RootDirectories.Count -eq 1) {
      $WrappedItems = @(Get-PaxPackageItems -Path $RootDirectories[0].FullName)
      $WrappedDirectories = @($WrappedItems | Where-Object { $_.PSIsContainer })
      $WrappedInstallers = @($WrappedItems | Where-Object { -not $_.PSIsContainer -and $_.Extension -in @(".exe", ".msi") })

      if ($WrappedInstallers.Count -gt 0 -and $WrappedDirectories.Count -eq 0) {
        Start-PaxInstaller -InstallerPath ($WrappedInstallers | Select-Object -First 1) -Name $Name
      }
      else {
        Write-Host "[+] Deploying portable package $Name..." -ForegroundColor Gray
        Deploy-PortableItems -Items (@($WrappedItems) + @($RootFiles)) -Config $Config -Name $Name
      }
    }
    else {
      Write-Host "[+] Deploying portable package $Name..." -ForegroundColor Gray
      Deploy-PortableItems -Items $RootItems -Config $Config -Name $Name
    }
  }
  finally {
    if (Test-Path $TempExtract) { Remove-Item $TempExtract -Recurse -Force }
  }

  Write-Host "`nDone." -ForegroundColor Green
}

## old pkg code
# param(
#   [string]$Action,
#   [string]$PackageName
# )

# function Invoke-PkgInstall {
#   $action = $args[0]
#   $query = $args[1]
#   $remainingArgs = $args | Select-Object -Skip 2
#   $packages = @{
#     "7z"  = "7zip.7zip"
#     "blender"  = "BlenderFoundation.Blender"
#     "discord" = "Discord.Discord"
#     "code"  = "Microsoft.VisualStudioCode"
#     "docker" = "Docker.DockerDesktop"
#     "dotnet8" = "Microsoft.DotNetDesktopRuntime.8"
#     "epic" = "EpicGames.EpicGamesLauncher"
#     "firefox" = "Mozilla.Firefox"
#     "gimp" = "GIMP.GIMP.3"
#     "git" = "Git.Git"
#     "itunes" = "Apple.iTunes"
#     "jbt" = "JetBrains.Toolbox"
#     "minecraft" = "Mojang.MinecraftLauncher"
#     "mixxx" = "MiXXX.MiXXX"
#     "node" = "OpenJS.NodeJS.LTS"
#     "obs" = "OBSProject.OBSStudio"
#     "python" = "Python.Python.3.12"
#     "riot" = "RiotGames.Valorant.NA"
#     "reaper" = "Cockos.REAPER"
#     "steam" = "Valve.Steam"
#     "sudo" = "Microsoft.Sudo"
#     "terminal" = "Microsoft.WindowsTerminal"
#     "vbox" = "Oracle.VirtualBox"
#     "vdj" = "AtomixProductions.VirtualDJ"
#     "vim" = "vim.vim"
#     "vs" = "Microsoft.VisualStudio.Community"
#     "wsl" = "Microsoft.WSL"
#   }
#   $wingetAction = switch ($action) {
#     "i" { "install" }
#     "s" { "search" }
#     default { $action }
#   }
#   if ($packages.ContainsKey($query)) {
#     $targetId = $packages[$query]
#   } else {
#     $targetId = $query
#   }
#   if ($wingetAction -eq "install") {
#     winget install -e --id $targetId --source winget $remainingArgs
#   } else {
#     winget $wingetAction $targetId $remainingArgs
#   }
# }

# function Scrub-Pkg {
#   # TODO
#   Get-AppxPackage -AllUsers Microsoft.XboxGamingOverlay | Remove-AppxPackage -AllUsers
#   Get-AppxPackage -AllUsers *WindowsTerminal* | Remove-AppxPackage -AllUsers
# }

# if (!$env:PKG) {
#   Write-Host "pax!"
#   pax $Action $PackageName
# }
# elseif ($PackageName) {
#   Invoke-PkgInstall $Action $PackageName
# }
# else {
#   Get-AppxPackage -AllUsers
# }

if ($Action -in @("i", "install")) {
  if ([string]::IsNullOrWhiteSpace($PackageName)) {
    throw "Package name is required. Use: pax.ps1 install <package>"
  }
  Invoke-PaxInstall -Name $PackageName
}
else {
  throw "Unsupported action '$Action'. Use: pax.ps1 install <package>"
}
