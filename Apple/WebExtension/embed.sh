#!/bin/bash

# Brew
export PATH="$PATH:/opt/homebrew/bin:/opt/brew/bin"

# Nix
export PATH="$PATH:/etc/profiles/per-user/$USER/bin"
if [[ -x $(command -v direnv) ]]; then
    export PATH="${PATH}:$(direnv exec . sh -c 'echo $PATH')"
fi

npm run --prefix "${PROJECT_DIR}/../Extension" package

BUILT_RESOURCES_DIR="${BUILT_PRODUCTS_DIR}/${UNLOCALIZED_RESOURCES_FOLDER_PATH}"
mkdir -p "${BUILT_RESOURCES_DIR}"
cp -R "${PROJECT_DIR}/../Extension/build/safari/" "${BUILT_RESOURCES_DIR}"
