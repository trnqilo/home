param(
  [Parameter(Mandatory=$true, Position=0)]
  [ValidateRange(0, 100)]
  [int]$Volume
)

$nircmd = Get-Command nircmd.exe -ErrorAction SilentlyContinue

if ($nircmd) {
  $val = [int]($Volume / 100.0 * 65535)
  & nircmd.exe setsysvolume $val
  Write-Host "Volume set to $Volume% (via nircmd)"
} else {
  Write-Error "nircmd not found on the PATH"
}
