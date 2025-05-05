package org.example.client;

import org.example.client.cli.ConsoleInput;
import org.example.client.cli.ConsoleOutput;
import org.example.client.command.ClientCommand;
import org.example.client.command.client_commands.*;
import org.example.client.managers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    static String host;
    static int port;
    static ConsoleOutput consoleOutput = new ConsoleOutput();
    static ConsoleInput consoleInput = new ConsoleInput();
    static RunnableScriptsManager runnableScriptsManager = new RunnableScriptsManager();
    static ClientCommandManager clientCommandManager = new ClientCommandManager();

    public static void main(String[] args) {
        if (args.length != 2) {
            consoleOutput.printError("Программа запускается с указанием 2 аргументов: <имя хоста> <порт>");
            return;
        }
        host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException numberFormatException) {
            consoleOutput.printError("порт - число!!");
            return;
        }
        if (port < 0 || port > 65535) {
            consoleOutput.printError("Некорректное значение порта: 0 < port < 65535");
            return;
        }

        SimpleClient client = new SimpleClient(host, port, 100, 10, consoleOutput, false);

        ArrayList<ClientCommand> commands = new ArrayList<>(List.of(
                new ExitCommand(),
                new LoginCommand(client, consoleInput, consoleOutput),
                new RegisterCommand(client, consoleInput, consoleOutput),
                new LogoutCommand(consoleOutput),
                new HelpClientCommand(consoleOutput, clientCommandManager)
        ));

        clientCommandManager.addCommands(commands);

        new RuntimeManager(consoleOutput, consoleInput, client, runnableScriptsManager, clientCommandManager).run();
    }
}
