package org.example.server.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class AsyncMultiThreadServer implements Runnable {
    private final int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private volatile boolean isRunning = false;

    public static int BUFFER_SIZE = 1024;

    public static Logger logger = LoggerFactory.getLogger(Server.class);

    public AsyncMultiThreadServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            openServerSocket();
        } catch (IOException ioException) {
            logger.error("Ошибка при открытии сервера");
        }

        logger.info("Сервер запущен");
        this.isRunning = true;
        while (isRunning) {
            try {
                selector.select(200);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (ClosedSelectorException e) {
                logger.error("Селектор закрыт: {}", e.getMessage());
                break;
            } catch (SocketException e) {
                logger.error("Ошибка сокетов: {}", e.getMessage());
            } catch (IOException ioException) {
                logger.error("Ошибка чтения запроса: {}", ioException.getMessage());
            }

            TaskManager.getReadyResults();

        }

        shutdown();

    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel keyChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = keyChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        new Thread(new ConnectionManager(clientChannel)).start();
    }

    private void openServerSocket() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        try {
            serverSocketChannel = serverSocketChannel.bind(new InetSocketAddress(port));
        } catch (IOException ioException) {
            logger.error("Недопустимый для прослушивания порт");
        }
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

    }

    private void shutdown() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            logger.info("Сервер остановлен");
        } catch (IOException e) {
            logger.error("Ошибка при попытке остановить работу сервака: {}", e.getMessage());
        }
    }

    public void stop() {
        this.isRunning = false;
    }

}
