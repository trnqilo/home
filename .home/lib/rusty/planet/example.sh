#!/usr/bin/env bash
set -e

server_command='cargo run'
client_command='sleep 2 && cargo run --example client'
mux triple "$server_command" "$client_command" "$client_command"
