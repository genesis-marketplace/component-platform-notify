#!/bin/bash
source /home/genesis-notify/.bashrc
systemctl start postgresql-14
su -c "source /home/genesis-notify/.bashrc ; yes | remap --commit" - "genesis-notify"
su -c "JvmRun global.genesis.environment.scripts.SendTable -t USER -f /home/genesis-notify/run/site-specific/data/user.csv" - "genesis-notify"
echo "remap done"
