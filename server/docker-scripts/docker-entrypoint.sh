#!/bin/bash
systemctl start postgresql-14
systemctl enable sshd.service
systemctl start sshd.service
su -c "startServer" - "genesis-notify"
echo "Logged as genesis-notify, starting server"
tail -f /dev/null