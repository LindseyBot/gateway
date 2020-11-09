package net.notfab.lindsey.core.framework.menu;

import lombok.Getter;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.Lindsey;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MenuManager extends ListenerAdapter {

    @Getter
    private static MenuManager Instance;

    private final Map<Long, Menu> menus = ExpiringMap.builder()
        .expiration(5, TimeUnit.MINUTES)
        .build();

    public MenuManager(Lindsey lindsey) {
        lindsey.addEventListener(this);
        Instance = this;
    }

    /* ---------------------------------------------------------------- */

    public void register(Menu menu) {
        this.menus.put(menu.getMessageId(), menu);
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        Menu menu = this.menus.get(event.getMessageIdLong());
        if (menu == null) {
            return;
        }
        menu.onReaction(event.getReaction(), event.getJDA());
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        if (event.getMember() == null || event.getMember().getUser().isBot()) {
            return;
        }
        Menu menu = this.menus.get(event.getMessageIdLong());
        if (menu == null) {
            return;
        }
        menu.onReaction(event.getReaction(), event.getJDA());
    }

}
