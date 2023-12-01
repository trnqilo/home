
```bash
# auto set /tmp to tmpfs
sysrc tmpmfs="YES"
```

```bash
mkdir -p /mnt/shm
mount -t tmpfs -o size=1g,mode=1777 tmpfs /mnt/shm
```

in /etc/fstab
```conf
tmpfs   /mnt/shm   tmpfs   rw,mode=1777,size=1g   0   0
```
