package net.notfab.lindsey.framework.command.help;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import net.notfab.lindsey.framework.menu.Menu;

import java.util.*;
import java.util.stream.Collectors;

public class HelpArticle {

    @Getter
    private final Map<String, HelpPage> pages = new HashMap<>();

    private HelpArticle(List<HelpPage> pages) {
        pages.forEach(page -> this.pages.put(page.getName().toLowerCase(), page));
    }

    public static HelpArticle of(HelpPage... pages) {
        return new HelpArticle(Arrays.asList(pages));
    }

    public HelpPage get(String name) {
        return this.pages.get(name.toLowerCase());
    }

    /**
     * Sends a formatted help page.
     *
     * @param channel - Channel to send in.
     * @param member  - Member that requested this help page.
     * @param args    - arguments provided by the user.
     * @param msg     - Messenger instance.
     * @param i18n    - Translator instance.
     */
    public void send(TextChannel channel, Member member, String[] args, Messenger msg, Translator i18n) {
        if (args.length == 0) {
            Menu.create(channel, pages.values().stream()
                .map(page -> page.asEmbed(i18n, member))
                .collect(Collectors.toList()));
        } else {
            HelpPage page = this.get(args[0]);
            if (page == null) {
                msg.send(channel, sender(member) + i18n.get(member, "core.help_nf"));
                return;
            }
            MessageEmbed embed = page.asEmbed(i18n, member);
            Menu.create(channel, Collections.singletonList(embed));
        }
    }

    private String sender(Member member) {
        return "**" + member.getEffectiveName() + "**";
    }

}
