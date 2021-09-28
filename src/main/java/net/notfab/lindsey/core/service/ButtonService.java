package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.lindseybot.controller.registry.ButtonRegistry;
import net.lindseybot.entities.interaction.buttons.ButtonMeta;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ButtonService {

    private final ButtonRegistry registry;

    public ButtonService(ButtonRegistry registry) {
        this.registry = registry;
        this.registry.fetchAll();
        log.info("Loaded " + registry.getAll().size() + " buttons.");
    }

    public boolean exists(String name) {
        return this.registry.exists(name);
    }

    public ButtonMeta get(String method) {
        return this.registry.get(method);
    }

}
