{
  description = "A clj-xgboost nix flake for Clojure dev env";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";

  outputs = { self, nixpkgs }:
    let
      javaVersion = 21;
      overlays = [
        (final: prev: rec {
          jdk = prev."jdk${toString javaVersion}";
          boot = prev.boot.override { inherit jdk; };
          clojure = prev.clojure.override { inherit jdk; };
          leiningen = prev.leiningen.override { inherit jdk; };
        })
      ];
      supportedSystems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
      forEachSupportedSystem = f: nixpkgs.lib.genAttrs supportedSystems (system: f {
        pkgs = import nixpkgs { inherit overlays system; };
      });
    in
      {
        devShells = forEachSupportedSystem ({ pkgs }: {
          default = pkgs.mkShell {
            packages = with pkgs; [
              boot
              clojure
              leiningen
              clojure-lsp
            ];

            shellHook =''
              echo "Welcome to the clj-xgboost env!"
            '';
          };
        });
      };
}
