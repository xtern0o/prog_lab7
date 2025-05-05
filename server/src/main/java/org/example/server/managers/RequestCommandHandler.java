package org.example.server.managers;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.exceptions.NoSuchCommand;
import org.example.server.command.Command;
import org.example.server.command.NoAuthCommand;
import org.example.server.utils.DatabaseSingleton;

/**
 * Middleware
 * Класс для обработки запросов с командами
 * @author maxkarn
 */
public class RequestCommandHandler {
    private final CommandManager commandManager;

    public RequestCommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Метод обработки запросов
     * Если все эщкере то делаем
     * Если нет команды или там с аргументами не то чето, то говорим
     * @param requestCommand request from lovely loved user
     * @return response
     */
    public Response handleRequestCommand(RequestCommand requestCommand) {
        try {
            Command command = commandManager.getCommand(requestCommand.getCommandName());
            if (command == null) throw new NoSuchCommand(requestCommand.getCommandName());

            if (!(command instanceof NoAuthCommand)) {
//                if (requestCommand.getUser() == null) {
//                    return new Response(ResponseStatus.LOGIN_REQUIRED, "Для выполнения этой команды необходима авторизация");
//                }
                if (!requestCommand.getUser().validate()) {
                    return new Response(ResponseStatus.VALIDATION_ERROR, "Failed user validation");
                }
                if (!DatabaseSingleton.getDatabaseManager().checkUserData(requestCommand.getUser())) {
                    return new Response(ResponseStatus.LOGIN_UNLUCK, "Неверные данные пользователя");
                }
            }

            return commandManager.execute(requestCommand);

        } catch (NoSuchCommand noSuchCommand) {
            return new Response(ResponseStatus.NO_SUCH_COMMAND, "Команда \"" + requestCommand.getCommandName() + "\" не найдена");
        } catch (IllegalArgumentException illegalArgumentException) {
            return new Response(ResponseStatus.ARGS_ERROR, "Неверное использование аргументов. " + illegalArgumentException.getMessage());
        }
    }
}
