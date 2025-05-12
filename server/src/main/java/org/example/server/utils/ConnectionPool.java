package org.example.server.utils;

import org.example.common.dtp.Response;

import java.nio.channels.SocketChannel;

public record ConnectionPool(Response response, SocketChannel clientChannel) {}
