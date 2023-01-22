{
  description = "Wallet";
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
  inputs.flake-utils.url = "github:numtide/flake-utils";
  inputs.android-nixpkgs.url = "github:tadfisher/android-nixpkgs";

  outputs = { self, nixpkgs, flake-utils, android-nixpkgs }:
    flake-utils.lib.simpleFlake {
      inherit self nixpkgs;
      name = "wallet";
      shell = { pkgs }:
        let
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
          pkgs.mkShell {
            buildInputs = [
              pkgs.nodejs-19_x
              pkgs.gitlint
              pkgs.temurin-bin-19
              pkgs.kotlin-native
              android-sdk
            ];
            shellHook = ''
              export PATH="$PWD/Extension/node_modules/.bin:$PWD/node_modules/.bin:$PATH"
            '';
          };
    };
}
