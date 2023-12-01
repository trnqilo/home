#!/usr/bin/env swift

import Foundation

struct Endpoint: Codable {
  let path: String
  let method: String
  let response: String
}

class SimpleHTTPServer {
  let port: UInt16
  var endpoints: [Endpoint] = []
  
  init(port: UInt16) {
    self.port = port
  }
  
  func loadEndpoints(from jsonFile: String) throws {
    let url = URL(fileURLWithPath: jsonFile)
    let data = try Data(contentsOf: url)
    endpoints = try JSONDecoder().decode([Endpoint].self, from: data)
  }
  
  func start() {
    let socket = try! ServerSocket(port: port)
    print("Server started on port \(port)")
    
    while true {
      let client = try! socket.accept()
      DispatchQueue.global().async {
        self.handleClient(client)
      }
    }
  }
  
  private func handleClient(_ client: Socket) {
    let request = client.readLine()
    let components = request.components(separatedBy: " ")
    guard components.count > 1 else { return }
    
    let method = components[0]
    let path = components[1].dropFirst()
    
    if let endpoint = endpoints.first(where: { $0.path == path && $0.method == method }) {
      let response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n\(endpoint.response)"
      client.write(response)
    } else {
      let response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nNot Found"
      client.write(response)
    }
    
    client.close()
  }
}

class ServerSocket {
  private let socketFD: Int32
  
  init(port: UInt16) throws {
    socketFD = socket(AF_INET, SOCK_STREAM, 0)
    var addr = sockaddr_in()
    addr.sin_family = sa_family_t(AF_INET)
    addr.sin_port = port.bigEndian
    addr.sin_addr.s_addr = INADDR_ANY
    let addrPointer = withUnsafePointer(to: &addr) {
      $0.withMemoryRebound(to: sockaddr.self, capacity: 1) { $0 }
    }
    guard bind(socketFD, addrPointer, socklen_t(MemoryLayout<sockaddr_in>.size)) == 0 else {
      throw SocketError.bindFailed
    }
    guard listen(socketFD, 10) == 0 else {
      throw SocketError.listenFailed
    }
  }
  
  func accept() throws -> Socket {
    var addr = sockaddr_in()
    var len = socklen_t(MemoryLayout<sockaddr_in>.size)
    let clientFD = withUnsafeMutablePointer(to: &addr) {
      $0.withMemoryRebound(to: sockaddr.self, capacity: 1) { addrPointer in
        Darwin.accept(socketFD, addrPointer, &len)
      }
    }
    guard clientFD > 0 else {
      throw SocketError.acceptFailed
    }
    return Socket(socketFD: clientFD)
  }
}

class Socket {
  private let socketFD: Int32
  
  init(socketFD: Int32) {
    self.socketFD = socketFD
  }
  
  func readLine() -> String {
    var line = ""
    var char: UInt8 = 0
    while recv(socketFD, &char, 1, 0) > 0 {
      if char == 13 { continue }
      if char == 10 { break }
      line.append(Character(UnicodeScalar(char)))
    }
    return line
  }
  
  func write(_ string: String) {
    _ = string.withCString { cs in
      Darwin.write(socketFD, cs, strlen(cs))
    }
  }
  
  func close() {
    Darwin.close(socketFD)
  }
}

enum SocketError: Error {
  case bindFailed
  case listenFailed
  case acceptFailed
}

func serve() {
  let server = SimpleHTTPServer(port: 8080)
  try server.loadEndpoints(from: "swift.json")
  server.start()
}

serve()
