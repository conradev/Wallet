#!/bin/zsh

brew install node openjdk@20

bun install

mkdir -p ~/Library/Java/JavaVirtualMachines
ln -sfn $(brew --prefix)/opt/openjdk/libexec/openjdk.jdk ~/Library/Java/JavaVirtualMachines/openjdk.jdk
