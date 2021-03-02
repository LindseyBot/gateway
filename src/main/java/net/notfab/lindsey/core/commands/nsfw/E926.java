package net.notfab.lindsey.core.commands.nsfw;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kodehawa.lib.imageboards.DefaultImageBoards;
import net.kodehawa.lib.imageboards.entities.BoardImage;
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

@Component
public class E926 implements Command {

    private static final Logger logger = LoggerFactory.getLogger(E926.class);

    private final Random random = new Random();

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("e926")
            .alias("safefurry")
            .module(Modules.NSFW)
            .permission("commands.e926", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        int page = Math.max(1, random.nextInt(25));
        if (args.length == 0) {
            DefaultImageBoards.E926.get(page, 1).async(safeFurryImages -> {
                BoardImage image = safeFurryImages.get(random.nextInt(safeFurryImages.size()));
                try {
                    buildEmbed(image, member, channel);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            DefaultImageBoards.E926.search(args[0]).async(safeFurryImages -> {
                BoardImage image = safeFurryImages.get(random.nextInt(safeFurryImages.size()));
                try {
                    buildEmbed(image, member, channel);
                } catch (IOException e) {
                    logger.error("Error while creating embed", e);
                }
            });
        }
        return true;
    }

    private void buildEmbed(BoardImage image, Member member, TextChannel channel) throws IOException {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(i18n.get(member, "commands.nsfw.load"), image.getURL())
            .setDescription("**" + i18n.get(member, "commands.nsfw.tags") + "**:" + image.getTags())
            .setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
                member.getUser().getEffectiveAvatarUrl())
            .addField(i18n.get(member, "commands.nsfw.rating"), image.getRating().toString(), true)
            .addField(i18n.get(member, "commands.nsfw.size"), image.getWidth() + "x" + image.getHeight(), true)
            .addField(i18n.get(member, "commands.nsfw.score"), Integer.toString(image.getScore()), true)
            .setImage(image.getURL());
        msg.send(channel, embed.build());
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("e926")
            .text("commands.nsfw.description")
            .usage("L!e926 [tag] [rating]")
            .permission("commands.e926")
            .addExample("L!e926")
            .addExample("L!safefurry cat");
        return HelpArticle.of(page);
    }

}
