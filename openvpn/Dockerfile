# docker build -t openvpn-server .
# docker run -d --name openvpn-server --cap-add=NET_ADMIN --net=host -v openvpn-data:/etc/openvpn openvpn-server
FROM alpine:latest

RUN apk add --no-cache openvpn easy-rsa iptables bash

VOLUME ["/etc/openvpn"]

EXPOSE 1194/udp

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

COPY client.sh /client.sh
RUN chmod +x /client.sh

ENTRYPOINT ["/entrypoint.sh"]
