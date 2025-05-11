package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

import java.text.SimpleDateFormat;

public class InfoCommand extends Command {
    private final CollectionManager collectionManager;

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public InfoCommand(CollectionManager collectionManager) {
        super("info", "вывод в стандартный поток вывода информации о коллекции (тип, дата инициализации, количество элементов и т.д.)");
        this.collectionManager = collectionManager;
    }


    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }
        String res =
                "Информация о коллекции:\n" + String.format(
                ": тип                  | %s\n" +
                ": количество элементов | %d\n" +
                ": дата инициализации   | %s",
                CollectionManager.getTypeOfCollection(),
                CollectionManager.getCollectionSize(),
                dateFormat.format(CollectionManager.getInitDate())
        );
        return new Response(ResponseStatus.OK, res);
    }
}
