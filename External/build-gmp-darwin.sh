#!/bin/bash

set -e

SDK_FILTER=$1

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"/gmp
BUILD_DIR="$(pwd)/../build"

build() {
    SDK_NAME=$1
    HOST=$2
    EXTRA_CFLAGS=$3
    CC="$(xcrun --sdk $SDK_NAME -f clang) -isysroot $(xcrun --sdk $SDK_NAME --show-sdk-path)"
    CC_FOR_BUILD="$(xcrun --sdk macosx -f clang) -isysroot $(xcrun --sdk macosx --show-sdk-path)"

    ./configure --disable-shared --host=$HOST \
        CC="$CC $EXTRA_CFLAGS" \
        CPP="$CC $EXTRA_CFLAGS -E" \
        CC_FOR_BUILD="$CC_FOR_BUILD" \
        CPP_FOR_BUILD="$CC_FOR_BUILD -E" \
        CFLAGS="-Wno-bitwise-conditional-parentheses -Wno-unused-value" || cat config.log
    
    make -j $(sysctl hw.logicalcpu | awk '{print $2}')

    SDK_DIR="${BUILD_DIR}/${SDK_NAME}"
    mkdir -p "${SDK_DIR}"
    cp .libs/libgmp.a "${SDK_DIR}/libgmp-$HOST.a"

    make distclean
}

combine() {
    SDK_NAME=$1
    SDK_DIR="${BUILD_DIR}/${SDK_NAME}"

    if [[ ! -z "$SDK_FILTER}" && "${SDK_FILTER}" != "${SDK_NAME}" ]]; then
        return
    fi

    xcrun --sdk $SDK_NAME lipo -create "${SDK_DIR}/libgmp-"*.a -output "${SDK_DIR}/libgmp.a"
    rm "${SDK_DIR}/libgmp-"*.a
}


archs() {
    OUTPUT_FILE="${BUILD_DIR}/$1/libgmp.a"
    if [[ -f "${OUTPUT_FILE}" ]]; then
        echo $(lipo -info "${OUTPUT_FILE}" | awk -F ": " '{print $NF}')
    fi
}

if [[ "$(archs macosx)" != "x86_64 arm64" && (-z "$SDK_FILTER" || "$SDK_FILTER" == "macosx") ]]; then
    build macosx 'aarch64-apple-darwin' '-arch arm64 -mmacos-version-min=10.7'
    build macosx 'x86_64-apple-darwin' '-arch x86_64 -mmacos-version-min=10.7'
    combine macosx
fi

if [[ "$(archs iphoneos)" != "arm64" && (-z "$SDK_FILTER" || "$SDK_FILTER" == "iphoneos") ]]; then
    build iphoneos 'aarch64-apple-darwin' '-arch arm64 -mios-version-min=7.0'
    combine iphoneos
fi

if [[ "$(archs iphonesimulator)" != "x86_64 arm64" && (-z "$SDK_FILTER" || "$SDK_FILTER" == "iphonesimulator") ]]; then
    build iphonesimulator 'aarch64-apple-darwin' '-arch arm64 -mios-simulator-version-min=7.0'
    build iphonesimulator 'x86_64-apple-darwin' '-arch x86_64 -mios-simulator-version-min=7.0'
    combine iphonesimulator
fi
