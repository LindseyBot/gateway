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

import java.util.Random;

public class Danbooru implements Command {

    private final Random random = new Random();

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
                buildEmbed(image, member, channel);
            });
        } else if (args.length == 1) {
            DefaultImageBoards.DANBOORU.search(args[0], r).async(danbooruImages -> {
                BoardImage image = danbooruImages.get(random.nextInt(danbooruImages.size()));
                buildEmbed(image, member, channel);
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
                buildEmbed(image, member, channel);
            });
        }
        return false;
    }

    private void buildEmbed(BoardImage image, Member member, TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("click here if image doesn't load", image.getURL())
                .setDescription("**Tags**:" + image.getTags())
                .setFooter("Requested by " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(),
                        member.getUser().getEffectiveAvatarUrl())
                .addField("Rating", image.getRating().toString(), true)
                .addField("Size", image.getWidth() + "x" + image.getHeight(), true)
                .addField("Score", Integer.toString(image.getScore()), true)
                .setImage(image.getURL());
        channel.sendMessage(embed.build()).queue();
    }

}
