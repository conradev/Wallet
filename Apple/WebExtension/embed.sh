#!/bin/bash

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/../.. >/dev/null 2>&1 && pwd)"

export PATH="$PATH:$HOME/.bun/bin:$PROJECT_DIR/node_modules/.bin"

cd "$PROJECT_DIR/Extension"

bun install
bun run package

BUILT_RESOURCES_DIR="$BUILT_PRODUCTS_DIR/$UNLOCALIZED_RESOURCES_FOLDER_PATH"
mkdir -p "$BUILT_RESOURCES_DIR"
cp -R "$PROJECT_DIR/Extension/build/safari/" "$BUILT_RESOURCES_DIR"
