$inputString = $args -join " "
if ([string]::IsNullOrWhiteSpace($inputString)) {
  Write-Host "Usage: clac [expression] OR clac [add|sub|mul|div|avg] [numbers...]" -ForegroundColor Yellow
  exit 1
}

$cleanInput = $inputString -replace '\$|,', '' -replace 'x', '*'

$cmd = $args[0]
$params = $args[1..($args.Count - 1)]

$expr = switch -Exact ($cmd) {
  "add" { $params -join "+" }
  "sub" { $params -join "-" }
  "mul" { $params -join "*" }
  "div" { $params -join "/" }
  "avg" { "(($($params -join '+')) / $($params.Count))" }
  Default { $null }
}

try {
  if ($expr) {
    $result = Invoke-Expression $expr
  } else {
    $result = Invoke-Expression $cleanInput
  }

  if ($result -is [double] -or $result -is [decimal]) {
    [math]::Round($result, 4)
  } else {
    $result
  }
} catch {
  Write-Host "[!] Calculation Error" -ForegroundColor Red
}
