package net.notfab.lindsey.core.framework.command;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Data
@AllArgsConstructor
public class MethodReference {

    private Object instance;
    private Method method;

    public void invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        this.method.invoke(instance, args);
    }

}
