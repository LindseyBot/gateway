package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.notfab.eventti.Event;
import net.notfab.eventti.EventManager;
import net.notfab.eventti.Listener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventService {

    private final EventManager eventManager;

    public EventService() {
        this.eventManager = new EventManager();
    }

    /**
     * Fires an event locally.
     *
     * @param event Event to fire.
     */
    public void fire(Event event) {
        this.eventManager.fire(event);
    }

    public void addListener(Listener listener) {
        this.eventManager.addListener(listener);
    }

}
