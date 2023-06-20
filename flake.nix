{
  description = "Wallet";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    android-nixpkgs.url = "github:tadfisher/android-nixpkgs";
  };

  outputs = { self, flake-utils, nixpkgs, android-nixpkgs, ... }:
    flake-utils.lib.eachDefaultSystem (system:
    let
      pkgs = nixpkgs.legacyPackages.${system};
      android-stable = pkgs.callPackage android-nixpkgs {
        channel = "stable";
      };
      android-sdk = android-stable.sdk (sdkPkgs: with sdkPkgs; [
        cmdline-tools-latest
        build-tools-33-0-0
        platform-tools
        platforms-android-33
      ]);
    in
    {
      devShell = pkgs.mkShell {
        buildInputs = [
          pkgs.bashInteractive
          pkgs.nodejs
          pkgs.temurin-bin
          pkgs.kotlin-native
          android-sdk
          pkgs.git
          pkgs.gitlint
        ];
        shellHook = ''
          export PATH="$PWD/Extension/node_modules/.bin:$PWD/node_modules/.bin:$PATH"
        '';
      };
    });
}
