package org.example.server.managers;

import org.example.common.dtp.ObjectSerializer;
import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.command.Command;
import org.example.server.utils.ConnectionPool;
import org.example.server.utils.RequestCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс ля запуска Runnable-таски по обработке соединений
 * Многопоточное чтение (Thread)
 * Многопоточная обработка (FixedThreadPool)
 */
public class ConnectionManager implements Runnable {
    private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(8);
    private final SocketChannel clientChannel;
    private final CommandManager commandManager;

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    public ConnectionManager(SocketChannel clientChannel, CommandManager commandManager) {
        this.clientChannel = clientChannel;
        this.commandManager = commandManager;
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = clientChannel.read(buffer)) > 0) {
                buffer.flip();
                byteStream.write(buffer.array(), 0, bytesRead);
                buffer.clear();
            }

            if (bytesRead == -1) {
                clientChannel.close();
                return;
            }

            byte[] receivedData = byteStream.toByteArray();

            // ignore stupid requests
            if (receivedData.length == 0) return;

            try {
                RequestCommand requestCommand = (RequestCommand) ObjectSerializer.deserializeObject(receivedData);

                logger.info(
                        "GOT REQUEST: CommandName: {}; Args: {}, User: {}",
                        requestCommand.getCommandName(), requestCommand.getArgs(), requestCommand.getUser()
                );

                TaskManager.addNewFuture(
                        fixedThreadPool.submit(
                                new RequestCommandHandler(
                                        requestCommand,
                                        clientChannel,
                                        commandManager
                                )
                        )
                );

            } catch (IOException e) {
                logger.warn("Неудача при десериализации: " + e.getMessage());
                sendNewResponse(new ConnectionPool(
                        new Response(ResponseStatus.COMMAND_ERROR, "Ошибка при десериализации"),
                        clientChannel
                ));
            } catch (ClassNotFoundException e) {
                logger.warn("Не удалось корректо десериализовать: " + e.getMessage());
                sendNewResponse(new ConnectionPool(
                        new Response(ResponseStatus.COMMAND_ERROR, "Ошибка при десериализации класса"),
                        clientChannel
                ));
            }

        } catch (IOException ioException) {
            logger.warn("Ошибка при буферизации: {}", ioException.getMessage());
            sendNewResponse(new ConnectionPool(
                    new Response(ResponseStatus.COMMAND_ERROR, "Ошибка при буферизации"),
                    clientChannel
            ));
        }
    }

    public static void sendNewResponse(ConnectionPool connectionPool) {
        new Thread(() -> {
            try {
                connectionPool.clientChannel().write(ByteBuffer.wrap(ObjectSerializer.serializeObject(connectionPool.response())));

                logger.info(
                        "SENT RESPONSE: [{}] {}",
                        connectionPool.response().getResponseStatus(), connectionPool.response().getMessage()
                );

            } catch (IOException ioException) {
                logger.warn("Не удалось отправить ответ клиенту: {}", ioException.getMessage());
            }
        }).start();
    }
}
