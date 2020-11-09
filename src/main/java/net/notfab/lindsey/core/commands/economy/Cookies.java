package net.notfab.lindsey.core.commands.economy;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.economy.EconomyService;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.framework.profile.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Cookies implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private ProfileManager profiles;

    @Autowired
    private EconomyService economy;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("cookies")
            .alias("cookie")
            .module(Modules.ECONOMY)
            .permission("commands.cookies", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            UserProfile profile = profiles.getUser(member);
            msg.send(channel, sender(member) + i18n.get(member, "commands.economy.cookies.self", profile.getCookies()));
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("daily")) {
                UserProfile profile = profiles.getUser(member);
                if (profile.getCookieStreak() == 0) {
                    // no streak
                    profile.setLastDailyCookies(System.currentTimeMillis());
                    profile.setCookieStreak(profile.getCookieStreak() + 1);
                    profiles.save(profile);
                    economy.pay(member, 15);
                    msg.send(channel, sender(member) + i18n.get(member, "commands.economy.cookies.daily", 15));
                } else if (isInLastHours(profile, 24)) {
                    String time = Utils.getTime(TimeUnit.DAYS.toMillis(1) -
                        (System.currentTimeMillis() - profile.getLastDailyCookies()), member, i18n);
                    msg.send(channel, sender(member) + i18n.get(member, "commands.economy.cookies.daily_already", time));
                    return false;
                } else {
                    // streak
                    if (isInLastHours(profile, 48)) {
                        profile.setCookieStreak(profile.getCookieStreak() + 1);
                    } else {
                        profile.setCookieStreak(1);
                    }
                    profile.setLastDailyCookies(System.currentTimeMillis());
                    long cookies = profile.getCookieStreak() * 15;
                    profiles.save(profile);
                    economy.pay(member, cookies);
                    msg.send(channel, sender(member) + i18n.get(member,
                        "commands.economy.cookies.daily_streak", cookies, profile.getCookieStreak()));
                }
                return true;
            }
            Member target = FinderUtil.findMember(args[0], message);
            if (target == null) {
                msg.send(channel, sender(member) + i18n.get(member, "search.member", args[0]));
                return false;
            } else {
                UserProfile profile = profiles.getUser(target);
                msg.send(channel, sender(member) + i18n.get(member, "commands.economy.cookies.target", target.getEffectiveName(), profile.getCookies()));
            }
        } else {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("cookies")
            .text("commands.economy.cookies.description")
            .usage("L!cookies <@user>")
            .permission("commands.cookies")
            .addExample("L!cookies")
            .addExample("L!cookies @Lindsey");
        return HelpArticle.of(page);
    }

    private boolean isInLastHours(UserProfile profile, long hours) {
        long lastDaily = profile.getLastDailyCookies();
        if (lastDaily == 0) {
            return false;
        }
        return (System.currentTimeMillis() - lastDaily) < TimeUnit.HOURS.toMillis(hours);
    }

}

