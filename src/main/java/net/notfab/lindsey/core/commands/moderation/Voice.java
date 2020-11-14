package net.notfab.lindsey.core.commands.moderation;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
class Voice implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("voice")
            .permission("commands.voice", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length < 3) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            if (args[0].equalsIgnoreCase("move")) {
                VoiceChannel sourceChannel = FinderUtil.findVoiceChannel(args[0], channel.getGuild());
                VoiceChannel targetChannel = FinderUtil.findVoiceChannel(args[1], channel.getGuild());
                if (targetChannel == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.voice", args[1]));
                    return false;
                } else if (sourceChannel != null) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.mod.voice.moving"));
                    for (Member voiceMember : sourceChannel.getMembers()) {
                        this.move(voiceMember, targetChannel, member, channel);
                    }
                } else {
                    Member targetMember = FinderUtil.findMember(args[0], message);
                    if (targetMember == null) {
                        msg.send(channel, sender(member) + i18n.get(member, "search.member", args[0]));
                        return false;
                    }
                    msg.send(channel, sender(member) + i18n.get(member, "commands.mod.voice.moving"));
                    this.move(targetMember, targetChannel, member, channel);
                }
            } else if (args[0].equalsIgnoreCase("split")) {
                VoiceChannel from = FinderUtil.findVoiceChannel(args[0], channel.getGuild());
                VoiceChannel to = FinderUtil.findVoiceChannel(args[1], channel.getGuild());
                if (from == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.voice", args[0]));
                    return false;
                } else if (to == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.voice", args[1]));
                    return false;
                }
                AtomicInteger i = new AtomicInteger();
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.voice.splitting"));
                from.getMembers().forEach(target -> {
                    if (i.get() % 2 == 0) {
                        this.move(target, to, member, channel);
                    }
                    i.getAndIncrement();
                });
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
        HelpPage page = new HelpPage("voice")
            .text("commands.mod.voice.description")
            .usage("L!voice <move/split> <user/channel>")
            .permission("commands.voice")
            .addExample("L!voice move @lindsey music-02")
            .addExample("L!voice move #music-02 #music-01")
            .addExample("L!voice split #music-02 #music-01");
        return HelpArticle.of(page);
    }

    private void move(Member target, VoiceChannel channel, Member admin, TextChannel textChannel) {
        channel.getGuild().moveVoiceMember(target, channel).queue((s) -> {
        }, (ex) -> {
            if (ex instanceof PermissionException) {
                msg.send(textChannel, sender(admin) + i18n.get(admin, "commands.mod.voice.permission_move", target.getEffectiveName()));
            } else {
                log.error("Error while moving target(s)", ex);
            }
        });
    }

}
