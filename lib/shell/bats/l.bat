@powershell -NoProfile -Command "Get-ChildItem -Force %* | Sort-Object LastWriteTime -Descending"
