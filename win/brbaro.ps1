powershell -Command "irm https://raw.githubusercontent.com/brbaro/home/refs/heads/main/Scripts/up.ps1 | iex"
bash -c "curl -fsSL https://raw.githubusercontent.com/trnqilo/home/refs/heads/${branch:-home}/readme | bash"
exit
