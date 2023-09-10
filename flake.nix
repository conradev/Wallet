{
  description = "Wallet";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, flake-utils, nixpkgs, ... }:
    flake-utils.lib.eachDefaultSystem (system:
    let
      pkgs = nixpkgs.legacyPackages.${system};
    in
    {
      devShell = pkgs.mkShell {
        buildInputs = with pkgs; [
          bun
          nodejs
          temurin-bin-20
          gitlint
        ];
        shellHook = ''
          export PATH="$PWD/Extension/node_modules/.bin:$PWD/node_modules/.bin:$PATH"
        '';
      };
    });
}
