#!/bin/bash

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

./build-gmp-darwin.sh $1
./build-ripemd160-darwin.sh $1
./build-secp256k1-darwin.sh $1
./build-xkcp-darwin.sh $1
