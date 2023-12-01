# wireless ap

```bash
# setup
apt install hostapd iw
ufw allow in on wlan0

# verify ap capability
iw list | grep 'index:\|* AP'

# edit conf
vim /etc/hostapd/hostapd.conf`
```

#### 2.4 GHz

```conf
interface=wlan0
bridge=br0
driver=nl80211

hw_mode=g
channel=2
# ht_capab=[HT40][SHORT-GI-20][DSSS_CCK-40]
ht_capab=[HT40+]
ieee80211n=1
ieee80211d=1
wmm_enabled=1
wme_enabled=1
country_code=US

auth_algs=3
ignore_broadcast_ssid=0
wpa=3
wpa_key_mgmt=WPA-PSK
wpa_pairwise=CCMP
# wpa_pairwise=TKIP
rsn_pairwise=CCMP
macaddr_acl=0
eap_reauth_period=360000000

ssid={{AP_SSID_NAME}}
wpa_passphrase={{AP_PASSWORD}}
```

#### 5 GHz

```conf
interface=wlan0
bridge=br0
driver=nl80211

hw_mode=a
channel=36
ht_capab=[HT40+]
vht_oper_chwidth=1
vht_oper_centr_freq_seg0_idx=42

ieee80211n=1
ieee80211ac=1
wmm_enabled=1
wme_enabled=1
country_code=US

auth_algs=3
ignore_broadcast_ssid=0
wpa=3
wpa_key_mgmt=WPA-PSK
wpa_pairwise=CCMP
# wpa_pairwise=TKIP
rsn_pairwise=CCMP
macaddr_acl=0
eap_reauth_period=360000000

ssid={{AP_SSID_NAME}}
wpa_passphrase={{AP_PASSWORD}}
```

```bash
# run hostapd
# with systemd
echo 'DAEMON_CONF="/etc/hostapd/hostapd.conf"' > /etc/default/hostapd
systemctl unmask hostapd
systemctl enable hostapd
systemctl start hostapd
# with shell command
sudo hostapd /etc/hostapd/hostapd.conf
```
