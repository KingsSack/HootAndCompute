{
  description = "Development environment";

  inputs.nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
  outputs = { nixpkgs, ... }:
  let
    pkgs = nixpkgs.legacyPackages.x86_64-linux;
  in {
    devShells.x86_64-linux.default = pkgs.mkShell {
      buildInputs = [
        # List packages here
        pkgs.git
        pkgs.corretto17
        pkgs.kotlin
        pkgs.gradle_8
        pkgs.android-tools
      ];
    };
  };
}
