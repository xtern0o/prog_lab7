package org.example.common.dtp;

import org.example.common.utils.Validatable;

import java.io.Serializable;

public record User(String login, String password) implements Serializable, Validatable {
    @Override
    public String toString() {
        return String.format("User(%s : %s)", login, password);
    }

    @Override
    public boolean validate() {
        return login.length() > 3 && password.length() >= 6;
    }
}
