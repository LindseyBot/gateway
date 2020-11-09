package net.notfab.lindsey.core.framework.waiter;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

@Slf4j
@Service
public class Waiter extends ListenerAdapter {

    private final TaskExecutor threadPool;
    private final List<WaitingEvent> waiting = new ArrayList<>();

    public Waiter(@Qualifier("eventWaiter") TaskExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public WaitingEvent forMessage(Function<Message, Boolean> condition, long timeInMillis) {
        WaitingEvent event = new WaitingEvent(condition);
        this.threadPool.execute(() -> {
            try {
                event.setThread(Thread.currentThread());
                waiting.add(event);
                LockSupport.parkUntil(System.currentTimeMillis() + timeInMillis);
                waiting.remove(event);
                if (event.getResult() == null) {
                    event.getTimeout().run();
                } else {
                    event.getSuccess().accept(event.getResult());
                }
            } catch (Exception ex) {
                log.error("Failed to wait for event", ex);
            }
        });
        return event;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        for (WaitingEvent waitingEvent : waiting) {
            Function<Message, Boolean> condition = waitingEvent.getCondition();
            if (condition.apply(event.getMessage())) {
                waitingEvent.setResult(event.getMessage());
                LockSupport.unpark(waitingEvent.getThread());
            }
        }
    }

}
