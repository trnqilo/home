# Rusty Playground

## setup

### environment

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh # install rust
playground/server/db.sh setup # create postgres db
cp playground/server/nginx.conf /location/of/nginx/confs # configure nginx
```

### server

an API server using _Axum_ and _Diesel_ offering POST, GET and DELETE endpoints at '/playground' 

```bash
cd playground/server && cargo run
```

### cli

a cli for the playground/client library (provides calls to server APIs using _Reqwest_)

```bash
cd playground/cli
cargo run # get playgrounds
cargo run post # create default playground
cargo run post '{"equipment": ["Swing","Swing"]}' 
cargo run delete
```

### web client

a web frontend for the client library using _Seed-Rs_

```bash
cd playground/web

# static host
cargo make verify
cargo make build_release
python3 -m 'http.server' '8000'

# dev server
cargo make serve
cargo make watch

open http://localhost
```

# Rusty Shed
## functional examples

### rx/rayon

```bash
cd shed/reactive_shed && cargo run
```

### tokio threading with glium/opengl

```bash
cd shed/concurrency_shed && cargo run
```

### yew.rs basic project

```bash
cd shed/yew_shed && trunk serve
```

## incomplete examples

### mobile shed

evaluation of the _UniFFI_ crate for binding with Kotlin and Swift

### bindgen and cpp

C++ interop sample code

+ _cpp_shed_ shows some examples of a macro dsl for embedding C++ in Rust
+ _bindgen_shed_ will generate a binding.rs for the code in wrapper.h

### rusty shed

a small project with some basic rust code
