```bash
vim /etc/ssh/sshd_config
```

```conf
PermitRootLogin no
PasswordAuthentication no
KbdInteractiveAuthentication no
AuthenticationMethods publickey
AllowUsers youruser
MaxAuthTries 3
LoginGraceTime 20
X11Forwarding no
AllowAgentForwarding no
# AllowTcpForwarding no  # for tunnels
```
