// swift-tools-version:5.5
import PackageDescription

let package = Package(
    name: "WebSocketProxy",
    platforms: [
        .macOS(.v10_15)
    ],
    dependencies: [
        .package(url: "https://github.com/apple/swift-nio.git", from: "2.0.0"),
        .package(url: "https://github.com/apple/swift-nio-extras.git", from: "1.0.0"),
        .package(url: "https://github.com/apple/swift-nio-ssl.git", from: "2.0.0"),
        .package(url: "https://github.com/apple/swift-nio-http2.git", from: "1.0.0"),
    ],
    targets: [
        .executableTarget(
            name: "WebSocketProxy",
            dependencies: [
                .product(name: "NIO", package: "swift-nio"),
                .product(name: "NIOHTTP1", package: "swift-nio"),
                .product(name: "NIOWebSocket", package: "swift-nio"),
                .product(name: "NIOExtras", package: "swift-nio-extras"),
                .product(name: "NIOSSL", package: "swift-nio-ssl"),
                .product(name: "NIOHTTP2", package: "swift-nio-http2"),
            ]),
        .testTarget(
            name: "WebSocketProxyTests",
            dependencies: ["WebSocketProxy"]),
    ]
)
