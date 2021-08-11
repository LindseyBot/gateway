package net.notfab.lindsey.core.framework;

import lombok.extern.slf4j.Slf4j;
import net.lindseybot.entities.discord.Label;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.shared.enums.Language;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DiscordAdapter {

    private final Translator i18n;

    public DiscordAdapter(Translator i18n) {
        this.i18n = i18n;
    }

    public String getLabel(Label msg, Language language) {
        if (msg.isLiteral()) {
            return msg.getName();
        } else {
            return this.i18n.get(language, msg.getName());
        }
    }

}
