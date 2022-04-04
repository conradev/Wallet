#!/bin/bash

set -e

SDK_FILTER=$1

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"/XKCP
BUILD_DIR="$(pwd)/../build"

build() {
    SDK_NAME=$1
    OUTPUT_FILE="${BUILD_DIR}/${SDK_NAME}/libXKCP.a"

    if [[ -f "${OUTPUT_FILE}" || (! -z "$SDK_FILTER" && "$SDK_FILTER" != "$SDK_NAME" ) ]]; then
        return
    fi

    xcrun --sdk $SDK_NAME clang -c \
        lib/high/Keccak/FIPS202/KeccakHash.c \
        lib/low/KeccakP-1600/plain-64bits/KeccakP-1600-opt64.c \
        lib/high/Keccak/KeccakSponge.c \
        -Ilib/common \
        -Ilib/low/common \
        -Ilib/low/KeccakP-1600/common \
        -Ilib/high/Keccak \
        -Ilib/high/Keccak/FIPS202 \
        -Ilib/low/KeccakP-1600/plain-64bits \
        -Ilib/low/KeccakP-1600/plain-64bits/u6 \
        -I../src/xkcp \
        -isysroot $(xcrun --sdk $SDK_NAME --show-sdk-path) \
        $2

    mkdir -p "${BUILD_DIR}/${SDK_NAME}"
    xcrun --sdk $SDK_NAME libtool -static -o $OUTPUT_FILE *.o
    rm *.o
}


build macosx '-arch arm64 -arch x86_64 -mmacos-version-min=10.7'
build iphoneos '-arch arm64 -fembed-bitcode -mios-version-min=7.0'
build iphonesimulator '-arch arm64 -arch x86_64 -mios-simulator-version-min=7.0'
