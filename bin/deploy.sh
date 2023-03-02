#!/bin/sh

HOST_USER="$1"
SERVER_HOST=34.237.171.245
USER_AND_HOST="$HOST_USER@$SERVER_HOST"

scp ./target/medegas.jar "$USER_AND_HOST:/home/$HOST_USER"
ssh "$USER_AND_HOST" "sudo mv '/home/$HOST_USER/medegas.jar' /srv/medegas/"
ssh "$USER_AND_HOST" 'sudo chown medegas:medegas /srv/medegas/medegas.jar'
ssh "$USER_AND_HOST" 'sudo systemctl restart medegas.service'
