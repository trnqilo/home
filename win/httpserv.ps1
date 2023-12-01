param(
  [int]$Port = 1234,
  [string]$BaseDir = $PWD
)

$l = New-Object Net.HttpListener
$l.Prefixes.Add("http://localhost:$Port/")

try {
  $l.Start()
  Write-Host "Serving '$BaseDir' on http://localhost:$Port" -ForegroundColor Cyan
} catch {
  Write-Host "Error: Port $Port already in use or access denied." -ForegroundColor Red
  exit
}

while ($l.IsListening) {
  $c = $l.GetContext()
  $r = $c.Response
  $rel = [uri]::UnescapeDataString($c.Request.Url.LocalPath).TrimStart('/')
  $path = [IO.Path]::GetFullPath((Join-Path $BaseDir $rel))

  if ($path -notlike ($BaseDir + '*')) {
    $r.StatusCode = 403
    $r.Close()
    continue
  }

  if (Test-Path $path -PathType Container) { $path = Join-Path $path "index.html" }

  if (Test-Path $path) {
    $buffer = [IO.File]::ReadAllBytes($path)
    $r.ContentLength64 = $buffer.Length
    $r.OutputStream.Write($buffer, 0, $buffer.Length)
  } else {
    $r.StatusCode = 404
  }
  $r.Close()
}
