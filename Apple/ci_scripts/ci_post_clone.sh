#!/bin/zsh

brew install node openjdk@19

mkdir -p ~/Library/Java/JavaVirtualMachines
ln -sfn $(brew --prefix)/opt/openjdk/libexec/openjdk.jdk ~/Library/Java/JavaVirtualMachines/openjdk.jdk
