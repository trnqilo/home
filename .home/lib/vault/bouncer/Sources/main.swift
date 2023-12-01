import NIO
import NIOHTTP1
import NIOWebSocket

class WebSocketProxy {
    private let group = MultiThreadedEventLoopGroup(numberOfThreads: System.coreCount)
    private var serverChannel1: Channel?
    private var serverChannel2: Channel?

    func start() throws {
        let bootstrap = ServerBootstrap(group: group)
            .serverChannelOption(ChannelOptions.backlog, value: 256)
            .serverChannelOption(ChannelOptions.socketOption(.so_reuseaddr), value: 1)
            .childChannelInitializer { channel in
                let httpHandler = HTTPServerHandler()
                let webSocketHandler = WebSocketHandler()
                return channel.pipeline.configureHTTPServerPipeline(withServerUpgrade: (upgraders: [httpHandler.upgrader], completionHandler: { context in
                    return context.pipeline.addHandler(webSocketHandler)
                })).flatMap {
                    channel.pipeline.addHandler(httpHandler)
                }
            }

        serverChannel1 = try bootstrap.bind(host: "localhost", port: 1234).wait()
        serverChannel2 = try bootstrap.bind(host: "localhost", port: 4321).wait()

        print("WebSocket proxy started on ports 1234 and 4321")

        try serverChannel1?.closeFuture.wait()
        try serverChannel2?.closeFuture.wait()
    }

    func stop() throws {
        try serverChannel1?.close().wait()
        try serverChannel2?.close().wait()
        try group.syncShutdownGracefully()
    }
}

class HTTPServerHandler: ChannelInboundHandler {
    typealias InboundIn = HTTPServerRequestPart
    typealias OutboundOut = HTTPServerResponsePart

    let upgrader = HTTPProtocolUpgrader(upgradeProtocol: "websocket", requiredUpgradeHeaders: ["Sec-WebSocket-Key"]) { channel, _ in
        return channel.eventLoop.makeSucceededFuture(())
    }

    func channelRead(context: ChannelHandlerContext, data: NIOAny) {
        let reqPart = self.unwrapInboundIn(data)

        switch reqPart {
        case .head(let header):
            if header.uri != "/websocket" {
                let response = HTTPResponseHead(version: header.version, status: .notFound)
                context.write(self.wrapOutboundOut(.head(response)), promise: nil)
                context.writeAndFlush(self.wrapOutboundOut(.end(nil)), promise: nil)
                context.close(promise: nil)
            }
        case .body, .end:
            break
        }
    }
}

class WebSocketHandler: ChannelInboundHandler {
    typealias InboundIn = WebSocketFrame
    typealias OutboundOut = WebSocketFrame

    private var otherClient: Channel?

    func channelActive(context: ChannelHandlerContext) {
        print("New WebSocket connection established")
    }

    func channelInactive(context: ChannelHandlerContext) {
        print("WebSocket connection closed")
        otherClient?.close(promise: nil)
    }

    func channelRead(context: ChannelHandlerContext, data: NIOAny) {
        let frame = self.unwrapInboundIn(data)

        switch frame.opcode {
        case .text:
            guard let text = frame.unmaskedData.getString(at: 0, length: frame.unmaskedData.readableBytes) else {
                return
            }
            print("Received message: \(text)")

            if otherClient == nil {
                let remotePort = context.channel.localAddress?.port == 1234 ? 4321 : 1234
                let bootstrap = ClientBootstrap(group: context.eventLoop)
                    .channelOption(ChannelOptions.socketOption(.so_reuseaddr), value: 1)
                    .channelInitializer { channel in
                        let httpHandler = HTTPClientHandler(context: context)
                        return channel.pipeline.addHTTPClientHandlers().flatMap {
                            channel.pipeline.addHandler(httpHandler)
                        }
                    }

                bootstrap.connect(host: "localhost", port: remotePort).whenSuccess { channel in
                    self.otherClient = channel
                    self.forwardMessage(text, to: channel)
                }
            } else {
                forwardMessage(text, to: otherClient!)
            }

        case .connectionClose:
            context.close(promise: nil)
        default:
            break
        }
    }

    private func forwardMessage(_ text: String, to channel: Channel) {
        print("Forwarding message: \(text)")
        var buffer = channel.allocator.buffer(capacity: text.utf8.count)
        buffer.writeString(text)
        let frame = WebSocketFrame(fin: true, opcode: .text, data: buffer)
        channel.writeAndFlush(frame, promise: nil)
    }
}

class HTTPClientHandler: ChannelInboundHandler {
    typealias InboundIn = HTTPClientResponsePart
    typealias OutboundOut = HTTPClientRequestPart

    private let context: ChannelHandlerContext

    init(context: ChannelHandlerContext) {
        self.context = context
    }

    func channelActive(context: ChannelHandlerContext) {
        var headers = HTTPHeaders()
        headers.add(name: "Host", value: "localhost")
        headers.add(name: "Upgrade", value: "websocket")
        headers.add(name: "Connection", value: "upgrade")
        headers.add(name: "Sec-WebSocket-Key", value: "dGhlIHNhbXBsZSBub25jZQ==")
        headers.add(name: "Sec-WebSocket-Version", value: "13")

        let requestHead = HTTPRequestHead(version: .http1_1, method: .GET, uri: "/websocket", headers: headers)
        context.write(self.wrapOutboundOut(.head(requestHead)), promise: nil)
        context.writeAndFlush(self.wrapOutboundOut(.end(nil)), promise: nil)
    }

    func channelRead(context: ChannelHandlerContext, data: NIOAny) {
        let responsePart = self.unwrapInboundIn(data)

        switch responsePart {
        case .head(let responseHead):
            guard responseHead.status == .switchingProtocols else {
                context.close(promise: nil)
                return
            }
        case .body:
            break
        case .end:
            context.pipeline.removeHandler(self).whenComplete { _ in
                let webSocketHandler = WebSocketHandler()
                context.pipeline.addHandler(webSocketHandler).whenComplete { _ in
                    self.context.pipeline.removeHandler(HTTPServerHandler.self).whenComplete { _ in
                        self.context.pipeline.addHandler(webSocketHandler)
                    }
                }
            }
        }
    }
}

let proxy = WebSocketProxy()
do {
    try proxy.start()
} catch {
    print("Error starting proxy: \(error)")
}
