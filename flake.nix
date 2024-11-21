{
  description = "bookmanager";
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=24.05";
    utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, utils }:
    utils.lib.eachDefaultSystem (system:
      let
        config = {
          allowUnfree = true;
        };
        overlays = [
          (final: prev: {
            sbt = prev.sbt.overrideAttrs (oldAttrs: {
              postPatch = "";
            });
          })
        ];
        pkgs = import nixpkgs {
          inherit system;
          overlays = overlays;
          config = config;
        };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            coursier
            scala_3
            scala-cli
            scalafmt
            ammonite
          ];
        };
      });
}
