function Set-SecureFirewallPolicy {
  Write-Host "[!] Configuring Firewall..." -ForegroundColor Cyan
  Set-NetFirewallProfile -All -DefaultInboundAction Block -DefaultOutboundAction Allow -Enabled True
  Write-Host "[+] Inbound connections blocked on all profiles." -ForegroundColor Green
  Write-Host "[+] Outbound connections remain allowed." -ForegroundColor Green
  Write-Host "[+] Firewall is active.`n" -ForegroundColor Green
}

function Show-FirewallStatus {
  Write-Host "Firewall Status:" -ForegroundColor Gray
  Get-NetFirewallProfile | Select-Object Name, Enabled, DefaultInboundAction, DefaultOutboundAction | Format-Table
}

# Set-SecureFirewallPolicy
Show-FirewallStatus
