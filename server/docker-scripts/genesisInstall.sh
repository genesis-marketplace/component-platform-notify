#!/bin/bash
source /home/genesis-notify/.bashrc
systemctl start postgresql-14
su -c "source /home/genesis-notify/.bashrc ; genesisInstall" - "genesis-notify"
echo "genesisInstall done"