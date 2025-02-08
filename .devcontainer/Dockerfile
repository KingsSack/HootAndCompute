FROM ubuntu:20.04

# Install prerequisites for nix and useful things
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    xz-utils

# Install nix
RUN curl -L https://nixos.org/nix/install | sh -s -- --daemon
