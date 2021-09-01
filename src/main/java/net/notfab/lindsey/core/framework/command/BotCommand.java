package net.notfab.lindsey.core.framework.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BotCommand {

    /**
     * Listening path for this command.
     *
     * @return path.
     */
    String value();

}
