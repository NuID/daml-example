#!/bin/sh

trap "kill 0" EXIT

./bin/build.sh
./bin/sandbox.sh &
./bin/navigator.sh &
./bin/server.sh &

wait
