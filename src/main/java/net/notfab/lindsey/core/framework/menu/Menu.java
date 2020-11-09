package net.notfab.lindsey.core.framework.menu;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.Emotes;

import java.util.List;

public class Menu {

    @Getter
    private final long messageId;
    private final long channelId;

    private final List<MessageEmbed> pages;
    private int page = 0;

    private Menu(long messageId, long channelId, List<MessageEmbed> pages) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.pages = pages;
    }

    // -- Reactions

    public static Menu create(TextChannel channel, List<MessageEmbed> pages) {
        Message message = channel.sendMessage(pages.get(0))
            .complete();
        if (pages.size() > 1) {
            message.addReaction(Emotes.LeftArrow.asReaction())
                .queue();
        }
        message.addReaction(Emotes.XCheck.asReaction())
            .queue();
        if (pages.size() > 1) {
            message.addReaction(Emotes.RightArrow.asReaction())
                .queue();
        }
        Menu menu = new Menu(message.getIdLong(), channel.getIdLong(), pages);
        MenuManager.getInstance().register(menu);
        return menu;
    }

    protected void onReaction(MessageReaction reaction, JDA api) {
        if (!reaction.getReactionEmote().isEmote()) {
            return;
        }
        TextChannel channel = api.getTextChannelById(this.channelId);
        if (channel == null) {
            return;
        }
        if (Emotes.XCheck.getId().equalsIgnoreCase(reaction.getReactionEmote().getId())) {
            channel.deleteMessageById(this.messageId)
                .queue();
            return;
        }
        if (!this.hasPagination()) {
            return;
        }
        if (Emotes.LeftArrow.getId().equals(reaction.getReactionEmote().getId())) {
            // Left Arrow
            MessageEmbed previous = this.previousPage();
            if (previous == null) {
                return;
            }
            Message msg = new MessageBuilder()
                .setEmbed(previous)
                .build();
            channel.editMessageById(this.messageId, msg)
                .queue();
        } else if (Emotes.RightArrow.getId().equals(reaction.getReactionEmote().getId())) {
            // Right Arrow
            MessageEmbed next = this.nextPage();
            if (next == null) {
                return;
            }
            Message msg = new MessageBuilder()
                .setEmbed(next)
                .build();
            channel.editMessageById(this.messageId, msg)
                .queue();
        }
    }

    // -- Pagination

    private boolean hasPagination() {
        return this.pages.size() > 1;
    }

    private MessageEmbed nextPage() {
        this.page++;
        if (page == this.pages.size()) {
            this.page = 0;
        }
        return this.pages.get(this.page);
    }

    private MessageEmbed previousPage() {
        this.page--;
        if (page < 0) {
            this.page = this.pages.size() - 1;
        }
        return this.pages.get(this.page);
    }

}
