package net.notfab.lindsey.core.commands.nsfw;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kodehawa.lib.imageboards.DefaultImageBoards;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.kodehawa.lib.imageboards.entities.Rating;
import net.notfab.lindsey.framework.command.Command;

import java.nio.channels.Channel;
import java.util.Random;

public class Rule34 implements Command {

    private final Random random = new Random();

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args) throws Exception {

        int page = Math.max(1, random.nextInt(25));
        if (args.length == 0) {
            DefaultImageBoards.RULE34.get(page, 1).async(rule34Images -> {
                BoardImage image = rule34Images.get(random.nextInt(rule34Images.size()));
                buildEmbed(image, member, channel);
            });
        } else {
            DefaultImageBoards.RULE34.search(page, 1, String.join(" ", args)).async(rule34Images -> {
                BoardImage image = rule34Images.get(random.nextInt(rule34Images.size()));
                buildEmbed(image, member, channel);
            });
        }

        return false;
    }

    private void buildEmbed(BoardImage image, Member member, TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("click here if image doesn't load", image.getURL())
                .setDescription("**Tags**:" + image.getTags())
                .setFooter("Requested by " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(), member.getUser().getEffectiveAvatarUrl())
                .addField("Rating", image.getRating().toString(), true)
                .addField("Size", image.getWidth() + "x" + image.getHeight(), true)
                .addField("Score", Integer.toString(image.getScore()), true)
                .setImage(image.getURL());

        channel.sendMessage(embed.build()).queue();
    }

}
