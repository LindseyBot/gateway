package net.notfab.lindsey.core.commands.nsfw;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kodehawa.lib.imageboards.DefaultImageBoards;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.kodehawa.lib.imageboards.entities.Rating;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;

@Component
public class Danbooru implements Command {

    private final Random random = new Random();

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("danbooru")
                .module(Modules.NSFW)
                .permission("commands.danbooru", "Permission to use the base command")
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {
        Rating r = Rating.QUESTIONABLE;
        int page = Math.max(1, random.nextInt(25));
        if (args.length == 0) {
            DefaultImageBoards.DANBOORU.get(page, 1, r).async(danbooruImages -> {
                BoardImage image = danbooruImages.get(random.nextInt(danbooruImages.size()));
                try {
                    buildEmbed(image, member, channel);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (args.length == 1) {
            DefaultImageBoards.DANBOORU.search(args[0], r).async(danbooruImages -> {
                BoardImage image = danbooruImages.get(random.nextInt(danbooruImages.size()));
                try {
                    buildEmbed(image, member, channel);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (args.length == 2) {
            switch (args[0]) {
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
            DefaultImageBoards.DANBOORU.search(args[1], r).async(danbooruImages -> {
                BoardImage image = danbooruImages.get(random.nextInt(danbooruImages.size()));
                try {
                    buildEmbed(image, member, channel);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return false;
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

}
