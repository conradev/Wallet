#!/bin/bash

set -e

SDK_FILTER=$1

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"/bitcoin
BUILD_DIR="$(pwd)/../build"

build() {
    SDK_NAME=$1
    OUTPUT_FILE="${BUILD_DIR}/${SDK_NAME}/libripemd160.a"

    if [[ -f "${OUTPUT_FILE}" || (! -z "$SDK_FILTER" && "$SDK_FILTER" != "$SDK_NAME" ) ]]; then
        return
    fi

    xcrun --sdk $SDK_NAME clang -c \
        src/crypto/ripemd160.cpp \
        ../src/ripemd160/ripemd160_c.cpp \
        -Isrc \
        -I../src/ripemd160 \
        -stdlib=libc++ \
        -isysroot $(xcrun --sdk $SDK_NAME --show-sdk-path) \
        $2

    mkdir -p "${BUILD_DIR}/${SDK_NAME}"
    xcrun --sdk $SDK_NAME libtool -static -o $OUTPUT_FILE *.o
    rm *.o
}


build macosx '-arch arm64 -arch x86_64 -mmacos-version-min=10.7'
build iphoneos '-arch arm64 -mios-version-min=7.0'
build iphonesimulator '-arch arm64 -arch x86_64 -mios-simulator-version-min=7.0'
