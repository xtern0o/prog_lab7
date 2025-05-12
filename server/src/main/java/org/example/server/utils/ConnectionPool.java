package org.example.server.utils;

import org.example.common.dtp.Response;

import java.io.ObjectOutputStream;

public record ConnectionPool(Response response, ObjectOutputStream objectOutputStream) {}
