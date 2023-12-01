generate_client() {
  client_name=$1
  cd /etc/openvpn/pki
  ./easyrsa --batch gen-req "$client_name" nopass
  ./easyrsa --batch sign-req client "$client_name"

  mkdir -p /etc/openvpn/clients
  cat << EOF > /etc/openvpn/clients/$client_name.ovpn
client
dev tun
proto udp
remote YOUR_SERVER_PUBLIC_IP 1194
resolv-retry infinite
nobind
persist-key
persist-tun
remote-cert-tls server
cipher AES-256-GCM
auth SHA256
tls-auth ta.key 1
<ca>
$(cat ca.crt)
</ca>
<cert>
$(cat issued/$client_name.crt)
</cert>
<key>
$(cat private/$client_name.key)
</key>
<tls-auth>
$(cat ta.key)
</tls-auth>
EOF
}

generate_client username
