package org.example.client.managers;

import org.example.common.dtp.User;


/**
 * Статик класс для хранения текущего пользователя клиента
 */
public class AuthManager {
    private static User currentUser;

    public static User getCurrentUser() {
        return AuthManager.currentUser;
    }

    public static void setCurrentUser(User newUser) {
        AuthManager.currentUser = newUser;
    }
}
