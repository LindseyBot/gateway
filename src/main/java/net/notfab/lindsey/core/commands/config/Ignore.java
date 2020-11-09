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
import net.notfab.lindsey.core.service.IgnoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Ignore implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private IgnoreService service;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("ignore")
            .module(Modules.CORE)
            .permission("commands.ignore", "permissions.command", false)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                List<MessageEmbed> pages = this.createList(member.getGuild(), member);
                if (pages.isEmpty()) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.core.ignore.empty"));
                    return true;
                }
                Menu.create(channel, pages);
                return true;
            }
            TextChannel target = FinderUtil.findTextChannel(argsToString(args, 0), message.getGuild());
            if (target == null) {
                msg.send(channel, sender(member) + i18n.get(member, "search.channel", argsToString(args, 0)));
                return false;
            }
            long guildId = message.getGuild().getIdLong();
            if (service.isIgnored(guildId, target.getIdLong())) {
                service.remove(guildId, target.getIdLong());
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.ignore.removed", target.getName()));
            } else {
                service.add(guildId, target.getIdLong());
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.ignore.added", target.getName()));
            }
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                // add(0) name(1)
                TextChannel target = FinderUtil.findTextChannel(argsToString(args, 1), message.getGuild());
                if (target == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.channel", argsToString(args, 1)));
                    return false;
                }
                long guildId = message.getGuild().getIdLong();
                if (service.isIgnored(guildId, target.getIdLong())) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.core.ignore.is_ignored", target.getName()));
                    return false;
                }
                service.add(guildId, target.getIdLong());
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.ignore.added", target.getName()));
            } else if (args[0].equalsIgnoreCase("remove")) {
                // remove(0) name(1)
                TextChannel target = FinderUtil.findTextChannel(argsToString(args, 1), message.getGuild());
                if (target == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.channel", argsToString(args, 1)));
                    return false;
                }
                long guildId = message.getGuild().getIdLong();
                if (!service.isIgnored(guildId, target.getIdLong())) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.core.ignore.is_not_ignored", target.getName()));
                    return false;
                }
                service.remove(guildId, target.getIdLong());
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.ignore.removed", target.getName()));
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
        HelpPage page = new HelpPage("ignore")
            .text("commands.core.ignore.description")
            .usage("L!ignore <name/add/remove> [name]")
            .permission("commands.ignore")
            .addExample("L!ignore #channel")
            .addExample("L!ignore add #channel")
            .addExample("L!ignore remove #channel");
        return HelpArticle.of(page);
    }

    private List<MessageEmbed> createList(Guild guild, Member member) {
        List<MessageEmbed> pages = new ArrayList<>();
        List<List<String>> channels = Utils.chopped(new ArrayList<>(this.service.getAll(guild.getIdLong())), 10);
        for (int i = 0; i < channels.size(); i++) {
            StringBuilder text = new StringBuilder();
            for (String channelId : channels.get(i)) {
                TextChannel channel = guild.getTextChannelById(channelId);
                if (channel == null) {
                    service.remove(guild.getIdLong(), Long.parseLong(channelId));
                    continue;
                }
                text.append("\n- `").append(channel.getName()).append("`");
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setDescription(text.toString());
            builder.setTitle(i18n.get(member, "embeds.ignored.title"));
            builder.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
            builder.setFooter(i18n.get(member, "embeds.ignored.page", i + 1, channels.size()), null);
            pages.add(builder.build());
        }
        return pages;
    }

}
