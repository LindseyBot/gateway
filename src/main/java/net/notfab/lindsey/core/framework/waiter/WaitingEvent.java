package net.notfab.lindsey.core.framework.waiter;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.notfab.lindsey.core.framework.Utils;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
public class WaitingEvent {

    private Function<Message, Boolean> condition;
    private Message result;
    private Thread thread;
    private Consumer<Message> success = Utils.noop();
    private Runnable timeout = Utils::noop;

    public WaitingEvent(Function<Message, Boolean> condition) {
        this.condition = condition;
    }

    public WaitingEvent success(Consumer<Message> consumer) {
        this.success = consumer;
        return this;
    }

    public WaitingEvent timeout(Runnable failure) {
        this.timeout = failure;
        return this;
    }

}
