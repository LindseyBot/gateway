package net.notfab.lindsey.core.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.Emotes;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.leaderboard.LeaderboardService;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.leaderboard.Leaderboard;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import net.notfab.lindsey.shared.enums.LeaderboardType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Top implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private LeaderboardService service;

    @Autowired
    private ProfileManager profiles;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("leaderboard")
            .alias("top", "lb")
            .permission("commands.leaderboard", "permissions.command")
            .module(Modules.ECONOMY)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        LeaderboardType type;
        if (args.length == 0) {
            type = LeaderboardType.COOKIES;
        } else {
            type = LeaderboardType.fromString(args[0]);
            if (type == null) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.economy.leaderboard.unknown_type",
                    Stream.of(LeaderboardType.values()).map(Enum::name).collect(Collectors.joining(", "))));
                return false;
            }
        }
        Page<Leaderboard> leaderboard = service.getLeaderboard(type, 0, 15);
        msg.send(channel, this.createPage(member, leaderboard, type));
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("leaderboard")
            .text("commands.economy.leaderboard.description")
            .usage("L!leaderboard [name]")
            .url("https://github.com/LindseyBot/core/wiki/commands-leaderboards")
            .permission("commands.leaderboard")
            .addExample("L!leaderboard")
            .addExample("L!leaderboard slot");
        return HelpArticle.of(page);
    }

    private MessageEmbed createPage(Member member, Page<Leaderboard> leaderboards, LeaderboardType type) {
        StringBuilder text = new StringBuilder();
        int position = 1;
        for (Leaderboard leaderboard : leaderboards) {
            String crown = "";
            if (position == 1) {
                crown = Emotes.Crown_1.asEmote() + " ";
            } else if (position == 2) {
                crown = Emotes.Crown_2.asEmote() + " ";
            } else if (position == 3) {
                crown = Emotes.Crown_3.asEmote() + " ";
            }

            UserProfile profile = profiles.getUser(leaderboard.getUser());
            text.append(i18n.get(member, "embeds.leaderboard.line", crown, position, profile.getName(), leaderboard.getCount()));
            text.append("\n");
            position++;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(text.toString());
        builder.setTitle(i18n.get(member, "embeds.leaderboard.title", type.getPrettyName()));
        builder.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
        return builder.build();
    }

}
