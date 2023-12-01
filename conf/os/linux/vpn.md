# vpn

## install packages

```bash
sudo apt update && sudo apt install openvpn easy-rsa -y
```

## configure server
```bash
sudo vim /etc/openvpn/server.conf
```

```conf
port 1234
proto tcp
dev tun
ca ca.crt
cert server.crt
key server.key
dh dh2048.pem
server 10.8.0.0 255.255.255.0
ifconfig-pool-persist ipp.txt
push "redirect-gateway def1 bypass-dhcp"
push "dhcp-option DNS 192.168.1.1"
keepalive 10 120
tls-auth ta.key 0
cipher AES-128-CBC
comp-lzo
max-clients 4
user ovpn
group ovpn
persist-key
persist-tun
status openvpn-status.log
verb 3
auth SHA256
key-direction 0
script-security 2
client-connect /etc/openvpn/clientconnect.sh
```

```bash
sudo vim /etc/openvpn/clientconnect.sh
```

```bash
#!/usr/bin/env bash

touch /home/ovpn/clients.txt

if ! grep -q "$trusted_ip" /home/ovpn/clients.txt; then

# curl -X POST --data-urlencode "payload={\"channel\": \"#CHANNEL_NAME\", \"username\": \"webhookbot\", \"text\": \"VPN connection made: $trusted_ip\", \"icon_emoji\": \":smiley:\"}" https://hooks.slack.com/services/SLACKWEBHOOK

echo "$trusted_ip" >> /home/ovpn/clients.txt

fi
exit 0
```


## create certs

```bash
make-cadir ~/openvpn-ca
cd ~/openvpn-ca

./easyrsa init-pki
./easyrsa build-ca
./easyrsa gen-req server nopass
./easyrsa sign-req server server
./easyrsa gen-dh
openvpn --genkey secret ta.key

sudo cp pki/ca.crt /etc/openvpn/
sudo cp pki/issued/server.crt /etc/openvpn/
sudo cp pki/private/server.key /etc/openvpn/
sudo cp pki/dh.pem /etc/openvpn/dh2048.pem
sudo cp ta.key /etc/openvpn/
```

## build client

```bash
cd ~/openvpn-ca
./easyrsa gen-req client1 nopass
./easyrsa sign-req client client1
mkdir -p ~/openvpn-clients/configs
vim ~/openvpn-clients/base.conf
```

```conf
client
dev tun
proto tcp
remote SERVER_IP 1234
resolv-retry infinite
nobind
user nobody
group nogroup
persist-key
persist-tun
remote-cert-tls server
cipher AES-128-CBC
auth SHA256
comp-lzo
key-direction 1
verb 3
```

```bash
vim ~/openvpn-clients/make_config.sh
chmod +x ~/openvpn-clients/make_config.sh
~/openvpn-clients/make_config.sh client1
```

```bash
#!/usr/bin/env bash

# ~/openvpn-clients/make_config.sh

KEY_DIR=~/openvpn-ca/pki
BASE_CONFIG=~/openvpn-clients/base.conf
OUTPUT_DIR=~/openvpn-clients/configs
CLIENT_NAME=$1

cat ${BASE_CONFIG} \
    <(echo -e '<ca>') \
    ${KEY_DIR}/ca.crt \
    <(echo -e '</ca>\n<cert>') \
    ${KEY_DIR}/issued/${CLIENT_NAME}.crt \
    <(echo -e '</cert>\n<key>') \
    ${KEY_DIR}/private/${CLIENT_NAME}.key \
    <(echo -e '</key>\n<tls-auth>') \
    ~/openvpn-ca/ta.key \
    <(echo -e '</tls-auth>') \
    > ${OUTPUT_DIR}/${CLIENT_NAME}.ovpn

echo "Generated profile at ${OUTPUT_DIR}/${CLIENT_NAME}.ovpn"
```

## prep and run

```bash
sudo useradd -r -s /usr/sbin/nologin ovpn
sudo mkdir -p /home/ovpn
sudo chown -R ovpn:ovpn /home/ovpn
sudo chmod +x /etc/openvpn/clientconnect.sh
sudo vim /etc/sysctl.conf # net.ipv4.ip_forward=1
sudo sysctl -p
sudo systemctl restart openvpn@server
```

# configure firewall

```bash
sudo vim /etc/ufw/before.rules
```

```conf
# OPENVPN NAT RULES
*nat
:POSTROUTING ACCEPT [0:0]
-A POSTROUTING -s 10.8.0.0/24 -o eth0 -j MASQUERADE
COMMIT

...
# not needed if FORWARD policy is is ACCEPT by default
-A ufw-before-forward -i tun+ -j ACCEPT
-A ufw-before-forward -o tun+ -j ACCEPT


```
