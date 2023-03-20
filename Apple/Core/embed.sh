#!/bin/bash

set -eo pipefail

# Nix
export PATH="$PATH:/etc/profiles/per-user/$USER/bin"
if [[ -x $(command -v direnv) ]]; then
    export JAVA_HOME="$(direnv exec . sh -c 'echo $JAVA_HOME')"
fi

# Brew
if [[ -z $JAVA_HOME ]]; then
    export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null)"
fi

../gradlew :shared:embedAndSignAppleFrameworkForXcode
