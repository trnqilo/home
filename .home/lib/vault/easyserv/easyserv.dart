#!/usr/bin/env dart

import 'dart:io';
import 'dart:convert';

final String configFile = 'server.json';
late Map<String, dynamic> apiConfig;

void main() async {
  try {
    apiConfig = await loadApiConfig();
    final server = await HttpServer.bind(InternetAddress.loopbackIPv4, 3000);
    print('Server listening on port 3000');

    await for (HttpRequest request in server) {
      handleRequest(request);
    }
  } catch (e) {
    print('Error starting server: $e');
  }
}

Future<String> loadJsonConfig() async => await File(configFile).readAsString();

Future<Map<String, dynamic>> loadApiConfig() async {
  final jsonString = """{
      "/search": {
        "method": "GET",
        "handler": "search"
      },
      "/getDetails": {
        "method": "GET",
        "handler": "getDetails"
      }
    }""";
  return json.decode(jsonString);
}

void handleRequest(HttpRequest request) {
  final path = request.uri.path;
  final method = request.method;

  if (apiConfig.containsKey(path) && apiConfig[path]['method'] == method) {
    final handler = apiConfig[path]['handler'];
    switch (handler) {
      case 'search':
        handleSearch(request);
        break;
      case 'getDetails':
        handleGetDetails(request);
        break;
      default:
        handleNotFound(request);
    }
  } else {
    handleNotFound(request);
  }
}

void handleSearch(HttpRequest request) {
  final query = request.uri.queryParameters['query'];
  if (query == null) {
    sendResponse(request, 400, {'error': 'Missing query parameter'});
    return;
  }

  final results = {'results': 'Search results for: $query'};
  sendResponse(request, 200, results);
}

void handleGetDetails(HttpRequest request) {
  final query = request.uri.queryParameters['query'];
  if (query == null) {
    sendResponse(request, 400, {'error': 'Missing query parameter'});
    return;
  }

  final details = {'details': 'Details for: $query'};
  sendResponse(request, 200, details);
}

void handleNotFound(HttpRequest request) {
  sendResponse(request, 404, {'error': 'Not found'});
}

void sendResponse(HttpRequest request, int statusCode, Map<String, dynamic> body) {
  request.response
    ..statusCode = statusCode
    ..headers.contentType = ContentType.json
    ..write(json.encode(body))
    ..close();
}
