package org.example.client.command.client_commands;

import org.example.client.command.ClientCommand;

public class ExitCommand extends ClientCommand {
    public ExitCommand() {
        super("exit", "Выход из программы");
    }

    @Override
    public void execute() {
        System.exit(0);
    }
}
