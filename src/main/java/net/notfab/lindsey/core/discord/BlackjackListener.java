package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.Emotes;
import net.notfab.lindsey.core.framework.models.BlackjackModel;
import net.notfab.lindsey.core.service.EconomyService;
import net.notfab.lindsey.core.service.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class BlackjackListener extends ListenerAdapter {

    @Autowired
    private Translator i18n;

    @Autowired
    private EconomyService economy;

    private final ExpiringMap<Long, BlackjackModel> cache;

    public BlackjackListener(Lindsey lindsey, ExpiringMap<Long, BlackjackModel> cache) {
        lindsey.addEventListener(this);
        this.cache = cache;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        start(event.getReactionEmote().getAsReactionCode(), event.getChannel(), event.getMessageIdLong(), event.getMember());
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        start(event.getReactionEmote().getAsReactionCode(), event.getChannel(), event.getMessageIdLong(), event.getMember());
    }

    private void start(String emote, TextChannel channel, long messageid, Member member) {
        if (!cache.containsKey(messageid)) {
            return;
        }

        BlackjackModel model = cache.get(messageid);
        if (model.getId() != member.getUser().getIdLong()) {
            return;
        }
        BlackjackModel.Result res = null;
        if (emote.equals(Emotes.Check.asReaction())) {
            boolean canContinue = model.next();
            if (!canContinue) {
                res = model.getResult();
            }
            channel.editMessageById(messageid, model.getMessage(member)).queue();
        }
        if (emote.equals(Emotes.XCheck.asReaction())) {
            model.end();
            res = model.getResult();
            channel.editMessageById(messageid, model.getMessage(member)).queue();
        }

        if (res != null) {
            this.cache.remove(messageid);
            switch (res) {
                case win -> {
                    economy.pay(member, model.getPrice() * 2);
                    channel.sendMessage(i18n.get(member, "commands.economy.win", model.getPrice() * 2)).queue();
                }
                case draw -> {
                    economy.pay(member, model.getPrice());
                    channel.sendMessage(i18n.get(member, "commands.fun.blackjack.draw")).queue();
                }
                case lost -> channel.sendMessage(i18n.get(member, "commands.fun.lost")).queue();
            }
        }
    }

}
