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
      dependencies = [
        pkgs.bashInteractive
        pkgs.nodejs-19_x
        pkgs.temurin-bin-19
        pkgs.kotlin-native
        android-sdk
      ];
      shell = pkgs.mkShell {
        buildInputs = dependencies ++ [
          pkgs.git
          pkgs.gitlint
        ];
        shellHook = ''
          export PATH="$PWD/Extension/node_modules/.bin:$PWD/node_modules/.bin:$PATH"
        '';
      };
      dockerImage = pkgs.dockerTools.buildImage {
        name = "wallet-dev";
        copyToRoot = dependencies ++ [
          pkgs.coreutils
          pkgs.findutils
          pkgs.gnused
        ];
        config = {
          Env = [ "JAVA_HOME=${pkgs.temurin-bin-19}" ];
          Cmd = [ "${pkgs.bash}/bin/bash" ];
        };
      };
    in
    {
      packages = {
        docker = dockerImage;
      };
      defaultPackage = dockerImage;
      devShell = shell;
    });
}
