package net.notfab.lindsey.core.framework.command.external;

import lombok.Getter;

@Getter
public class BadArgumentException extends Exception {

    private final String message;
    private final Object[] args;

    public BadArgumentException(String message, Object... args) {
        this.message = message;
        this.args = args;
    }

}
