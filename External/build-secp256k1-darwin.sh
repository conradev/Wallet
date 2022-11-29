#!/bin/bash

set -e

SDK_FILTER=$1

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"/secp256k1
BUILD_DIR="$(pwd)/../build"

build() {
    SDK_NAME=$1
    OUTPUT_FILE="${BUILD_DIR}/${SDK_NAME}/libsecp256k1.a"

    if [[ -f "${OUTPUT_FILE}" || (! -z "$SDK_FILTER" && "$SDK_FILTER" != "$SDK_NAME" ) ]]; then
        return
    fi

    xcrun --sdk $SDK_NAME clang -c \
        src/secp256k1.c \
        src/precomputed_ecmult.c \
        src/precomputed_ecmult_gen.c \
        -DUSE_BASIC_CONFIG=1 \
        -DENABLE_MODULE_RECOVERY=1 \
        -isysroot $(xcrun --sdk $SDK_NAME --show-sdk-path) \
        $2

    mkdir -p "${BUILD_DIR}/${SDK_NAME}"
    xcrun --sdk $SDK_NAME libtool -static -o $OUTPUT_FILE *.o
    rm *.o
}


build macosx '-arch arm64 -arch x86_64 -mmacos-version-min=10.7'
build iphoneos '-arch arm64 -mios-version-min=7.0'
build iphonesimulator '-arch arm64 -arch x86_64 -mios-simulator-version-min=7.0'
