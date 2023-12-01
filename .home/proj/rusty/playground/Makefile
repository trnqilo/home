server:
	cd playground/server && cargo run
get:
	cd playground/cli && cargo run
post_full:
	cd playground/cli && cargo run post
post_small:
	cd playground/cli && cargo run post '{"equipment": ["Swing","Swing"]}'
web_client:
	cd playground/web && cargo make verify && cargo make build_release && python -m 'http.server' '8000'
rx_shed:
	cd shed/reactive_shed && cargo run
concurrency_shed:
	cd shed/concurrency_shed && cargo run
yew_shed:
	cd shed/yew_shed && trunk serve
