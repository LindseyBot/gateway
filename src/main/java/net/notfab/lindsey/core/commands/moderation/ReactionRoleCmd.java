package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.menu.Menu;
import net.notfab.lindsey.core.service.ReactionRoleService;
import net.notfab.lindsey.shared.entities.ReactionRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReactionRoleCmd implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private ReactionRoleService service;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("reactionroles")
            .alias("reactionrole", "rroles")
            .permission("commands.reactionroles", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else if (args.length == 1) {
            if (!args[0].equalsIgnoreCase("list")) {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
                return false;
            }
            // list
            List<MessageEmbed> pages = this.createList(member.getGuild(), member);
            if (pages.isEmpty()) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.reactionroles.empty"));
                return true;
            }
            Menu.create(channel, pages);
        } else if (args.length == 2) {
            if (!args[0].equalsIgnoreCase("remove")) {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
                return false;
            }
            // remove <name>
            String name = argsToString(args, 1).toLowerCase();
            boolean isRemoved = service.remove(member.getGuild(), name);
            if (isRemoved) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.reactionroles.removed", name));
            } else {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.reactionroles.failed_remove", name));
            }
        } else {
            // create <name> <role>
            if (!args[0].equalsIgnoreCase("create")) {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
                return false;
            }
            Role role = FinderUtil.findRole(argsToString(args, 2), member.getGuild());
            if (role == null) {
                msg.send(channel, sender(member) + i18n.get(member, "search.role", argsToString(args, 2)));
                return false;
            }
            String name = args[1].toLowerCase().replaceAll("\\s+", "_");
            if (name.length() > 18) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.reactionroles.failed_name_too_big"));
                return false;
            }
            boolean isStarted = service.create(role, name, member, channel);
            if (isStarted) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.reactionroles.started"));
            } else {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.reactionroles.failed_create"));
            }
        }
        return true;
    }

    private List<MessageEmbed> createList(Guild guild, Member member) {
        List<MessageEmbed> pages = new ArrayList<>();
        List<List<ReactionRole>> reactionRoles = Utils.chopped(new ArrayList<>(service.findAll(member.getGuild())), 10);
        for (int i = 0; i < reactionRoles.size(); i++) {
            StringBuilder text = new StringBuilder();
            for (ReactionRole reactionRole : reactionRoles.get(i)) {
                Role role = guild.getRoleById(reactionRole.getRoleId());
                TextChannel channel = guild.getTextChannelById(reactionRole.getChannelId());
                if (channel == null || role == null) {
                    service.remove(guild, reactionRole.getName());
                    continue;
                }
                String reaction = reactionRole.getReaction();
                if (reaction.contains(":")) {
                    reaction = "<:" + reaction + ">";
                }
                text.append("\n- **").append(reactionRole.getName()).append("**: ")
                    .append(i18n.get(member, "embeds.reactionroles.line", reaction, channel.getAsMention(), role.getName()));
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setDescription(text.toString());
            builder.setTitle(i18n.get(member, "embeds.reactionroles.title"));
            builder.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
            builder.setFooter(i18n.get(member, "embeds.reactionroles.page", i + 1, reactionRoles.size()), null);
            pages.add(builder.build());
        }
        return pages;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("reactionroles")
            .text("commands.mod.reactionroles.description")
            .usage("L!rroles <list/create/remove> [name] [role]")
            .url("https://github.com/LindseyBot/core/wiki/commands-reaction-roles")
            .permission("commands.reactionroles")
            .addExample("L!rroles list")
            .addExample("L!rroles create verified Verified")
            .addExample("L!rroles remove verified");
        return HelpArticle.of(page);
    }

}
