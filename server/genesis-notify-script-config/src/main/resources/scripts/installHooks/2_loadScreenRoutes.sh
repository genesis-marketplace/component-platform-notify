#!/bin/bash

# Source aliases and expand them
source "$GENESIS_HOME/genesis/util/setup.sh"
shopt -s expand_aliases

# Create dump folder
mkdir -p "$GENESIS_HOME/runtime/ScreenNotifyRouteExt/dump"

# Dump table contents
cd "$GENESIS_HOME/runtime/ScreenNotifyRouteExt/dump/"
DumpIt -t "SCREEN_NOTIFY_ROUTE_EXT"

# Insert data
{
  SendIt -t "SCREEN_NOTIFY_ROUTE_EXT" -f "$GENESIS_HOME/genesis-notify/data/SCREEN_NOTIFY_ROUTE_EXT.csv"
} ||
{
  # Exit code 2 will cause the script to log a delayed status and will run again after a remap
  exit 2
}

STATUS=$?
exit $STATUS
