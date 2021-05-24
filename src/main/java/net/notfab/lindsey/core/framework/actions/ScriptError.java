package net.notfab.lindsey.core.framework.actions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScriptError extends Exception {

    private Throwable cause;
    private Type type;

    public static enum Type {
        EXCEPTION,
        TIMEOUT,
        INTERRUPTION
    }

}
