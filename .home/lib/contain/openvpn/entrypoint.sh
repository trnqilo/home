#!/bin/bash

configure_networking() {
  INTERFACE=$(ip route | grep default | awk '{print $5}')
  iptables -t nat -A POSTROUTING -s 10.8.0.0/24 -o "$INTERFACE" -j MASQUERADE
  echo 1 > /proc/sys/net/ipv4/ip_forward
}

initialize_pki() {
  if [ ! -f /etc/openvpn/pki/ca.crt ]; then
    cd /etc/openvpn
    make-cadir pki
    cd pki
    ./easyrsa init-pki
    ./easyrsa --batch build-ca nopass
    ./easyrsa --batch gen-req server nopass
    ./easyrsa --batch sign-req server server
    ./easyrsa gen-dh
    openvpn --genkey secret ta.key
  fi
}

create_server_config() {
  if [ ! -f /etc/openvpn/server.conf ]; then
    cat << 'EOF' > /etc/openvpn/server.conf
port 1194
proto udp
dev tun
ca pki/ca.crt
cert pki/issued/server.crt
key pki/private/server.key
dh pki/dh.pem
server 10.8.0.0 255.255.255.0
ifconfig-pool-persist ipp.txt
keepalive 10 120
tls-auth pki/ta.key 0
cipher AES-256-GCM
auth SHA256
user nobody
group nogroup
persist-key
persist-tun
status openvpn-status.log
verb 3
explicit-exit-notify 1
EOF
  fi
}

start_server() {
  cd /etc/openvpn
  openvpn --config server.conf
}

configure_networking
initialize_pki
create_server_config
start_server
