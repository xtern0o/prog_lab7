package org.example.server.managers;

import org.example.common.dtp.RequestCommand;
import org.example.server.utils.ConnectionPool;
import org.example.server.utils.RequestCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private final SocketChannel clientSocket;

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    public ConnectionManager(SocketChannel socketChannel) {
        this.clientSocket = socketChannel;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream clientReader = new ObjectInputStream(clientSocket.socket().getInputStream());
            ObjectOutputStream clientWriter = new ObjectOutputStream(clientSocket.socket().getOutputStream());

            while (clientSocket.isConnected()) {
                RequestCommand requestCommand = (RequestCommand) clientReader.readObject();

                TaskManager.addNewFuture(fixedThreadPool.submit(new RequestCommandHandler(requestCommand, clientWriter)));
            }
        } catch (IOException ioException) {
            logger.warn(ioException.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("Ошибка при чтении полученных данных");
        }
    }

    public static void sendNewResponse(ConnectionPool connectionPool) {
        new Thread(() -> {
            try {
                connectionPool.objectOutputStream().writeObject(connectionPool.response());
                connectionPool.objectOutputStream().flush();
            } catch (IOException ioException) {
                logger.warn("Не удалось отправить ответ клиенту: {}", ioException.getMessage());
            }
        }).start();
    }
}
