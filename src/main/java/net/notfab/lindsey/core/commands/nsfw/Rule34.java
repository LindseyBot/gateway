package net.notfab.lindsey.core.commands.nsfw;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kodehawa.lib.imageboards.DefaultImageBoards;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.notfab.lindsey.core.framework.Emotes;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;
import java.util.stream.Stream;

@Component
public class Rule34 implements Command {

    private static final Logger logger = LoggerFactory.getLogger(Rule34.class);

    private final Random random = new Random();

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("rule34")
            .module(Modules.NSFW)
            .permission("commands.rule34", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (channel.isNSFW()) {
            int page = Math.max(1, random.nextInt(25));
            if (args.length == 0) {
                DefaultImageBoards.RULE34.get(page, 1).async(rule34Images -> {
                    BoardImage image = rule34Images.get(random.nextInt(rule34Images.size()));
                    try {
                        buildEmbed(image, member, channel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                if (Stream.of(args).anyMatch(f -> f.equalsIgnoreCase("loli"))) {
                    msg.send(channel, sender(member) + Emotes.CopThink.asEmote());
                    return true;
                }
                DefaultImageBoards.RULE34.search(page, 1, String.join(" ", args)).async(rule34Images -> {
                    BoardImage image = rule34Images.get(random.nextInt(rule34Images.size()));
                    try {
                        buildEmbed(image, member, channel);
                    } catch (IOException e) {
                        logger.error("Error while creating embed", e);
                    }
                });
            }
            return true;
        } else {
            msg.send(channel, i18n.get(member, "core.not_nsfw"));
            return false;
        }
    }

    private void buildEmbed(BoardImage image, Member member, TextChannel channel) throws IOException {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(i18n.get(member, "commands.nsfw.load"), image.getURL())
            .setDescription("**" + i18n.get(member, "commands.nsfw.tags") + "**:" + image.getTags())
            .setFooter(i18n.get(member, "commands.nsfw.request") + " " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(),
                member.getUser().getEffectiveAvatarUrl())
            .addField(i18n.get(member, "commands.nsfw.rating"), image.getRating().toString(), true)
            .addField(i18n.get(member, "commands.nsfw.size"), image.getWidth() + "x" + image.getHeight(), true)
            .addField(i18n.get(member, "commands.nsfw.score"), Integer.toString(image.getScore()), true)
            .setImage(image.getURL());
        msg.send(channel, embed.build());
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("rule34")
            .text("commands.nsfw.description.rule34")
            .usage("L!rule34 [tag]")
            .permission("commands.rule34")
            .addExample("L!rule34")
            .addExample("L!rule34 megumin");
        return HelpArticle.of(page);
    }

}
