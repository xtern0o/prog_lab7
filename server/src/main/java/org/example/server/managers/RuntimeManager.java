package org.example.server.managers;

import lombok.AllArgsConstructor;
import org.example.common.dtp.User;
import org.example.common.utils.Printable;
import org.example.server.utils.DatabaseSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Класс для управления жизненным циклом сервера (запуск, завершение работы)
 * @author maxkarn
 */
@AllArgsConstructor
public class RuntimeManager implements Runnable {
    private final Printable consoleOutput;
    private final AsyncMultiThreadServer server;

    public static final Logger logger = LoggerFactory.getLogger(RuntimeManager.class);

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveCollection();
            server.stop();

            logger.info("⚡ Сервер - В С Ё.");
        }));


        logger.info("Сервер для управления коллекцией Ticket запущен");
        consoleOutput.println("> [Ctrl + C], чтобы завершить работу");

        server.run();

    }

    public void saveCollection() {
        try {
            DatabaseSingleton.getDatabaseManager().commit();
            consoleOutput.println("Изменения в БД подтверждены");
        } catch (SQLException sqlException) {
            consoleOutput.println("Не удалось подтвердить изменения в БД");
        }
    }
}
