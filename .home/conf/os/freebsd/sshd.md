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
UseDNS no
# AllowTcpForwarding no  # for tunnels
```

```bash
# access alerts
chown root:wheel /usr/local/sbin/alert.sh
chmod 700 /usr/local/sbin/alert.sh
```

```conf
# add to the end of session in /etc/pam.d/sshd
session		optional	pam_exec.so	/usr/local/sbin/alert.sh
```

```bash
#!/usr/bin/env bash
# alert.sh
set -e

count=$(/bin/pgrep -f 'sshd.*\[priv\]' | /usr/bin/wc -l | /usr/bin/tr -d ' ' || true)
if [ "$PAM_SM_FUNC" = "pam_sm_close_session" ]; then
  count=$((count - 1))
fi

message="$PAM_SM_FUNC: $PAM_USER@$HOSTNAME [$PAM_RHOST] (active sessions: $count)"

/usr/local/bin/curl -s --max-time 5 -X POST \
  --data-urlencode "payload={\"channel\": \"#channel_name\", \"username\": \"webhookbot\", \"text\": \"${message}\", \"icon_emoji\": \":ghost:\"}" \
  'https://hooks.slack.com/services/ONE/23/abcdefg' >/dev/null || true
```
