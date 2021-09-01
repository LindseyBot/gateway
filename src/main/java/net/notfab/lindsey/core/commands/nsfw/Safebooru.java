package net.notfab.lindsey.core.commands.nsfw;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kodehawa.lib.imageboards.DefaultImageBoards;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.notfab.lindsey.core.framework.command.Command;
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
public class Safebooru implements Command {

    private static final Logger logger = LoggerFactory.getLogger(Safebooru.class);

    private final Random random = new Random();

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("safebooru")
            .module(Modules.NSFW)
            .permission("commands.safebooru", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        int page = Math.max(1, random.nextInt(25));
        if (args.length == 0) {
            DefaultImageBoards.SAFEBOORU.get(page, 1).async(safebooruImages -> {
                BoardImage image = safebooruImages.get(random.nextInt(safebooruImages.size()));
                try {
                    buildEmbed(image, member, channel);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            DefaultImageBoards.SAFEBOORU.search(args[0]).async(safeFurryImages -> {
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
        HelpPage page = new HelpPage("safebooru")
            .text("commands.nsfw.description")
            .usage("L!safebooru [tag] [rating]")
            .permission("commands.safebooru")
            .addExample("L!safebooru")
            .addExample("L!safebooru cat");
        return HelpArticle.of(page);
    }

}
