package net.notfab.lindsey.core.commands.config;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.menu.Menu;
import net.notfab.lindsey.core.framework.options.Option;
import net.notfab.lindsey.core.framework.options.OptionManager;
import net.notfab.lindsey.core.framework.options.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Options implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private OptionManager options;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("options")
            .alias("opts")
            .module(Modules.CORE)
            .permission("commands.options", "permissions.command", false)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                List<MessageEmbed> pages = this.createList(member);
                Menu.create(channel, pages);
            } else {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
                return false;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get")) {
                // get(0) name(1) value(2)
                Option option = options.find(args[1]);
                if (option == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.core.options.not_found"));
                    return false;
                }
                Object current = option.get(member.getGuild());
                String value;
                if (option.getType() == OptionType.TEXT_CHANNEL) {
                    value = ((TextChannel) current).getAsMention();
                } else if (option.getType() == OptionType.VOICE_CHANNEL) {
                    value = ((VoiceChannel) current).getName();
                } else {
                    value = String.valueOf(current);
                }
                if (value.equals(option.getFallback())) {
                    value = null;
                }
                msg.send(channel, this.createEmbed(member, option, value));
            } else if (args[0].equalsIgnoreCase("clear")) {
                // clear(0) name(1)
                Option option = options.find(args[1]);
                if (option == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.core.options.not_found"));
                    return false;
                }
                option.set(member.getGuild(), option.getFallback());
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.options.updated", option.getName()));
            } else {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
                return false;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                // set(0) name(1) value(2)
                Option option = options.find(args[1]);
                if (option == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.core.options.not_found"));
                    return false;
                }
                GuildChannel target = null;
                if (option.getType() == OptionType.TEXT_CHANNEL) {
                    target = FinderUtil.findTextChannel(args[2], member.getGuild());
                } else if (option.getType() == OptionType.VOICE_CHANNEL) {
                    target = FinderUtil.findVoiceChannel(args[2], member.getGuild());
                }
                try {
                    Object value;
                    if (target == null) {
                        value = option.getType().parse(member.getGuild(), args[2]);
                    } else {
                        value = option.getType().parse(member.getGuild(), target);
                    }
                    option.set(member.getGuild(), value);
                } catch (IllegalArgumentException ex) {
                    msg.send(channel, sender(member) + i18n.get(member, ex.getMessage()));
                    return false;
                }
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.options.updated", option.getName()));
            } else {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
                return false;
            }
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("options")
            .text("commands.core.options.description")
            .usage("L!options <list/get/set> [name] [value]")
            .permission("commands.options")
            .addExample("L!options list")
            .addExample("L!options get antiad.enabled")
            .addExample("L!options clear antiad.enabled")
            .addExample("L!options set antiad.enabled true");
        return HelpArticle.of(page);
    }

    private MessageEmbed createEmbed(Member member, Option option, String value) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(i18n.get(member, "options.embed.title", option.getName()), null);
        builder.setDescription(i18n.get(member, option.getDescription()));
        builder.addField(i18n.get(member, "options.embed.type"), option.getType().name(), true);
        builder.addField(i18n.get(member, "options.embed.default"), option.getFallback(), true);
        if (value != null) {
            builder.addField(i18n.get(member, "options.embed.current"), value, false);
        }
        builder.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
        return builder.build();
    }

    private List<MessageEmbed> createList(Member member) {
        List<MessageEmbed> pages = new ArrayList<>();
        List<List<Option>> options = Utils.chopped(this.options.getAll(), 10);
        for (int i = 0; i < options.size(); i++) {
            StringBuilder text = new StringBuilder();
            for (Option option : options.get(i)) {
                text.append("\n- `").append(option.getName()).append("`");
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setDescription(text.toString());
            builder.setTitle(i18n.get(member, "options.embed.list"));
            builder.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
            builder.setFooter(i18n.get(member, "options.embed.page", i + 1, options.size()), null);
            pages.add(builder.build());
        }
        return pages;
    }

}
