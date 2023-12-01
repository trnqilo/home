@echo off
REM powershell Get-Process -IncludeUserName
tasklist -fi "USERNAME eq %~1"
