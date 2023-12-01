#!/usr/bin/env bash
set -e

function _hs_init {
  if ! (type motion && type curl && type screen) &>/dev/null; then
    pkg i motion curl screen -y
    # pkg i psmisc -y # debian: killall command
  fi

  sudo rm -vf /usr/local/bin/_hs_*
  sudo rm -vf /usr/local/bin/hs
  sudo cp -vr "$DOTHOME/lib/housesitter/usr/local" /usr

  # if ! type _hs_dbu &>/dev/null; then
  #   cd
  #   git clone https://github.com/andreafabrizi/dropbox-uploader dbu
  #   sudo mv ./dbu/dropbox_uploader.sh /usr/local/bin/_hs_dbu
  #   rm -fr ./dbu
  # fi


  if ! [[ -f '/etc/default/housesitter' ]]; then
    sudo cp -v "$DOTHOME/lib/housesitter/etc/default/housesitter" /etc/default
  fi

  source /etc/default/housesitter
  if [[ "$USE_SYSTEMD" != true ]]; then
    sudo systemctl stop motion ||:
    sudo systemctl disable motion ||:
    sudo systemctl mask motion ||:
  fi
  if [[ -f '/etc/motion/motion.conf' && ! -f '/etc/motion/motion.conf.original' ]]; then
    sudo mv /etc/motion/motion.conf /etc/motion/motion.conf.original
  fi
  sudo cp -vr "$DOTHOME/lib/housesitter/etc/motion" /etc
  sudo su -c "echo \"`camera_config`\" >> /etc/motion/motion.conf"
}

function camera_config {
  if [[ "$CAMERA" == 'rtsp://'* ]]; then
    # eg. rtsp://NETCAM_USER:NETCAM_PASS@NETCAM_URL
    # netcam_userpass NETCAM_USER:NETCAM_PASS
    echo "videodevice none
netcam_url ${CAMERA}:554/stream2
netcam_params rtsp_transport=tcp
netcam_high_url ${CAMERA}:554/stream1
netcam_high_params rtsp_transport=tcp"
  elif [[ "$CAMERA" ]]; then
    echo "video_device ${CAMERA}"
  else
    echo 'video_device /dev/video0'
  fi
}

function _hs_ { echo 'hello'; }

if [[ "$hsmock" == 'true' ]]; then
  function _hs_stat { echo hsmock stat called at `this_time`; sleep 3; }
  function _hs_start { echo hsmock start called at `this_time`; sleep 3; }
  function _hs_stop { echo hsmock stop called at `this_time`; sleep 3; }
  function this_time { date +"%H:%M:%S"; }
fi

_hs_$@
