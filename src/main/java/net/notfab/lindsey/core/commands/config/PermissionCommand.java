package net.notfab.lindsey.core.commands.config;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.*;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import net.notfab.lindsey.framework.permissions.MemberPermission;
import net.notfab.lindsey.framework.permissions.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionCommand implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private PermissionManager permissions;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("permissions")
            .alias("perms")
            .permission("commands.permissions", "permissions.command", false)
            .module(Modules.CORE)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get")) {
                // get(0) role(1)
                Role role = FinderUtil.findRole(this.argsToString(args, 1), channel.getGuild());
                if (role == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.role"));
                    return false;
                }
                List<MemberPermission> perms = permissions.list(role);
                StringBuilder sb = new StringBuilder();
                sb.append(i18n.get(member, "commands.core.permissions.for", role.getName().replace("@", "")));
                for (MemberPermission node : perms) {
                    sb.append("\n").append("- ").append(node.getNode()).append(" : ").append(node.isAllowed());
                }
                msg.send(channel, sender(member) + sb.toString());
            } else if (args[0].equalsIgnoreCase("clear")) {
                // reset(0) role(1)
                Role role = FinderUtil.findRole(this.argsToString(args, 1), channel.getGuild());
                if (role == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.role"));
                    return false;
                }
                permissions.delete(role);
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.permissions.cleared"));
            } else {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
                return false;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("reset")) {
                // reset(0) role(1) name(2)
                Role role = FinderUtil.findRole(this.argsToString(args, 1), channel.getGuild());
                if (role == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.role"));
                    return false;
                }
                String name = args[2];
                if (!permissions.exists(name)) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.permission", name));
                    return false;
                }
                permissions.delete(role, name);
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.permissions.updated"));
            } else {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
                return false;
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set")) {
                // set(0) role(1) name(2) status(3)
                Role role = FinderUtil.findRole(args[1], channel.getGuild());
                if (role == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.role", args[1]));
                    return false;
                }
                String name = args[2];
                if (!permissions.exists(name)) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.permission", name));
                    return false;
                }
                boolean status = Boolean.parseBoolean(args[3]);
                permissions.set(role, name, status);
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.permissions.updated"));
            } else {
                HelpArticle article = this.help(member);
                article.send(channel, member, args, msg, i18n);
            }
        } else {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("permissions")
            .text("commands.core.permissions.description")
            .usage("L!perms <get,clear,reset,set> [role] [node] [value]")
            .permission("commands.permissions")
            .addExample("L!perms get mods")
            .addExample("L!perms clear mods")
            .addExample("L!perms reset mods commands.kick")
            .addExample("L!perms set mods commands.kick true");
        return HelpArticle.of(page);
    }

}
