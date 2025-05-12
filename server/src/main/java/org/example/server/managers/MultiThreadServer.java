package org.example.server.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadServer implements Runnable {
    private final int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private final CommandManager commandManager;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private volatile boolean isRunning = false;

    public static final Logger logger = LoggerFactory.getLogger(MultiThreadServer.class);

    public MultiThreadServer(int port, CommandManager commandManager) {
        this.port = port;
        this.commandManager = commandManager;
    }

    @Override
    public void run() {
        try {
            openServerSocket();
        } catch (IOException ioException) {
            logger.error("Ошибка при открытии сервера", ioException);
            return;
        }

        this.isRunning = true;
        logger.info("Сервер прослушивает порт: " + port);

        while (isRunning) {
            try {
                // Ожидание событий
                selector.select(200);

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException ioException) {
                logger.error("Ошибка в рантайме: ", ioException);
            }

            TaskManager.getReadyResults();
        }

        shutdown();
    }

    private void openServerSocket() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false); // Неблокирующий режим
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        logger.info("Серверный сокет открыт и готов принимать подключения");
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel keyChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = keyChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        logger.info("Новое подключение: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        // Обработка чтения запроса в пуле потоков
        new Thread(new ConnectionManager(clientChannel, commandManager)).start();
    }

    private void shutdown() {
        try {
            isRunning = false;
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            threadPool.shutdown();
            logger.info("Сервер остановлен");
        } catch (IOException e) {
            logger.error("Ошибка при остановке сервера", e);
        }
    }

    public void stop() {
        this.isRunning = false;
        selector.wakeup(); // Прерываем блокировку select()
    }
}