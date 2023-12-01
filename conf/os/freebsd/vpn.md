```bash
pkg install openvpn easy-rsa
mkdir -p /usr/local/etc/openvpn
cp /usr/local/share/examples/openvpn/sample-config-files/server.conf /usr/local/etc/openvpn/openvpn.conf
cp -r /usr/local/share/easy-rsa /usr/local/etc/openvpn/easy-rsa
cd /usr/local/etc/openvpn/easy-rsa
./easyrsa init-pki
./easyrsa build-ca
./easyrsa build-server-full server nopass
./easyrsa gen-dh
sysrc -f /etc/sysctl.conf net.inet.ip.forwarding=1
sysrc openvpn_enable="YES"
openvpn --genkey secret /usr/local/etc/openvpn/ta.key
vim /usr/local/etc/openvpn/openvpn.conf
service openvpn start
```

```conf /usr/local/etc/openvpn/openvpn.conf
# Port and Protocol
port 1194
proto udp
dev tun

# Cryptographic Keys & Certificates
ca /usr/local/etc/openvpn/easy-rsa/pki/ca.crt
cert /usr/local/etc/openvpn/easy-rsa/pki/issued/server.crt
key /usr/local/etc/openvpn/easy-rsa/pki/private/server.key
dh /usr/local/etc/openvpn/easy-rsa/pki/dh.pem

# Network Settings
server 10.8.0.0 255.255.255.0
ifconfig-pool-persist /usr/local/etc/openvpn/ipp.txt

# Routing and DNS Options
# Push traffic through the VPN
push "redirect-gateway def1 bypass-dhcp"
# Push public DNS servers (Cloudflare / Google)
push "dhcp-option DNS 1.1.1.1"
push "dhcp-option DNS 8.8.8.8"

# Connection Settings
keepalive 10 120
tls-auth /usr/local/etc/openvpn/ta.key 0
cipher AES-256-GCM
auth SHA256

# Permissions
user nobody
group nobody

# Persistence and Logging
persist-key
persist-tun
status /var/log/openvpn-status.log
log-append /var/log/openvpn.log
verb 3
explicit-exit-notify 1

```
