package net.notfab.lindsey.core.commands.nsfw;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kodehawa.lib.imageboards.DefaultImageBoards;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.kodehawa.lib.imageboards.entities.Rating;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;

@Component
public class E621 implements Command {

    private static final Logger logger = LoggerFactory.getLogger(E621.class);

    private final Random random = new Random();

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("e621")
            .alias("furry")
            .module(Modules.NSFW)
            .permission("commands.e621", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (channel.isNSFW()) {
            Rating r = Rating.QUESTIONABLE;
            int page = Math.max(1, random.nextInt(25));
            if (args.length == 0) {
                DefaultImageBoards.E621.get(page, 1, r).async(furryImages -> {
                    BoardImage image = furryImages.get(random.nextInt(furryImages.size()));
                    try {
                        buildEmbed(image, member, channel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                if (args.length == 2) {
                    switch (args[1]) {
                        case "safe":
                        case "s":
                            r = Rating.SAFE;
                            break;
                        case "explicit":
                        case "e":
                            r = Rating.EXPLICIT;
                            break;
                        case "questionable":
                        case "q":
                            r = Rating.QUESTIONABLE;
                            break;
                    }
                }

                DefaultImageBoards.E621.search(args[0], r).async(furryImages -> {
                    BoardImage image = furryImages.get(random.nextInt(furryImages.size()));
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
        HelpPage page = new HelpPage("e621")
            .text("commands.nsfw.description.e621")
            .usage("L!e621 [tag] [rating]")
            .permission("commands.e621")
            .addExample("L!e621")
            .addExample("L!furry cat")
            .addExample("L!e621 cat safe");
        return HelpArticle.of(page);
    }

}
