{ pkgs ? import <nixpkgs> }:
pkgs.mkShell {
  buildInputs = with pkgs; [
    nodejs-19_x
    gitlint
    android-tools
  ];
  shellHook = ''
    export PATH="$PWD/Extension/node_modules/.bin:$PWD/node_modules/.bin:$PATH"
  '';
}
