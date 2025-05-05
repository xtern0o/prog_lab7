package org.example.common.dtp;

import java.io.Serializable;

public record User(String login, String password) implements Serializable {
    @Override
    public String toString() {
        return String.format("User(%s : %s)", login, password);
    }

    public boolean validate() {
        return login.length() > 3 && password.length() >= 6;
    }
}
