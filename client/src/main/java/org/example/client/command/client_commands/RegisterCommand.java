package org.example.client.command.client_commands;

import org.example.client.builders.UserBuilder;
import org.example.client.cli.ConsoleInput;
import org.example.client.cli.ConsoleOutput;
import org.example.client.command.ClientCommand;
import org.example.client.managers.AuthManager;
import org.example.client.managers.SimpleClient;
import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.dtp.User;

import java.util.Objects;

public class RegisterCommand extends ClientCommand {
    private final SimpleClient client;
    private final ConsoleInput consoleInput;
    private final ConsoleOutput consoleOutput;

    public RegisterCommand(SimpleClient client, ConsoleInput consoleInput, ConsoleOutput consoleOutput) {
        super("register", "Регистрация нового пользователя");
        this.client = client;
        this.consoleInput = consoleInput;
        this.consoleOutput = consoleOutput;
    }

    @Override
    public void execute() {
        if (!Objects.isNull(AuthManager.getCurrentUser())) {
            consoleOutput.println("Вы уже авторизованы как \"" + AuthManager.getCurrentUser().login() + "\"");
            return;
        }

        consoleOutput.println("* Регистрация");

        User user = new UserBuilder(consoleOutput, consoleInput).build();

        RequestCommand requestCommand = new RequestCommand("register", user);
        Response response = client.send(requestCommand);

        if (response.getResponseStatus().equals(ResponseStatus.OK)) {
            AuthManager.setCurrentUser(user);
            consoleOutput.println("Регистрация прошла успешно! Вы авторизованы как " + user.login());
        }
        else {
            consoleOutput.printError(response.getMessage());
        }
    }
}
