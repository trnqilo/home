function Get-SteamHome {
  if ($env:STEAM_HOME) {
    return $env:STEAM_HOME
  }

  $customPath = "C:\Users\steam\steam"
  if (Test-Path $customPath) {
    return $customPath
  }

  $registryPath = "HKCU:\Software\Valve\Steam"
  $regPath = Get-ItemPropertyValue -Path $registryPath -Name "SteamPath" -ErrorAction SilentlyContinue
  if ($regPath) {
    return $regPath
  }

  return "$env:ProgramFiles\Steam"
}

function Get-CS2CfgDirectory {
  param($SteamHome)
  $relativeCfgPath = "steamapps\common\Counter-Strike Global Offensive\game\csgo\cfg"
  return Join-Path $SteamHome $relativeCfgPath
}

function Download-Config {
  param($Url, $Destination)
  try {
    Write-Host "[+] Downloading $(Split-Path $Destination -Leaf)..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $Url -OutFile $Destination -UseBasicParsing -ErrorAction Stop
  } catch {
    Write-Host "[!] Failed to download from $Url" -ForegroundColor Red
  }
}

function Sync-CS2Configs {
  $steamHome = Get-SteamHome
  $cs2CfgDir = Get-CS2CfgDirectory -SteamHome $steamHome

  $urlBase = "https://raw.githubusercontent.com/trnqilo/home/refs/heads/lib/play"
  $urlAutoexec = "$urlBase/Counter-Strike%20Global%20Offensive/game/csgo/cfg/autoexec.cfg"
  $urlSource2 = "$urlBase/Source2/source2.cfg"

  Write-Host "[+] Steam Home detected at: $steamHome" -ForegroundColor Gray
  Write-Host "[+] Target Cfg: $cs2CfgDir" -ForegroundColor Cyan

  if (!(Test-Path $cs2CfgDir)) {
    Write-Host "[!] Error: CS2 Config directory not found. Is CS2 installed?" -ForegroundColor Red
    return
  }

  Download-Config -Url $urlAutoexec -Destination (Join-Path $cs2CfgDir "autoexec.cfg")
  Download-Config -Url $urlSource2 -Destination (Join-Path $cs2CfgDir "source2.cfg")

  Write-Host "[+] Sync Complete! Bárbaro! 🚀" -ForegroundColor Green
}

Sync-CS2Configs
