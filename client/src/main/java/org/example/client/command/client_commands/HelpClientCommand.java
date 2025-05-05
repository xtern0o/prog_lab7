package org.example.client.command.client_commands;

import org.example.client.cli.ConsoleOutput;
import org.example.client.command.ClientCommand;
import org.example.client.managers.ClientCommandManager;

import java.util.stream.Collectors;

public class HelpClientCommand extends ClientCommand {
    private final ConsoleOutput consoleOutput;
    private final ClientCommandManager clientCommandManager;

    public HelpClientCommand(ConsoleOutput consoleOutput, ClientCommandManager clientCommandManager) {
        super("help_client", "Справка о доступных клиентских командах");
        this.consoleOutput = consoleOutput;
        this.clientCommandManager = clientCommandManager;
    }

    @Override
    public void execute() {
        consoleOutput.println("Справка по доступным клиентским командам");
        clientCommandManager.getCommands()
                .values()
                .forEach(command -> consoleOutput.println(command.toString()));
    }
}
