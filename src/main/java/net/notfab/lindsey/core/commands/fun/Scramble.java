package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.economy.EconomyService;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import net.notfab.lindsey.framework.profile.ProfileManager;
import net.notfab.lindsey.framework.profile.UserProfile;
import net.notfab.lindsey.framework.waiter.Waiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class Scramble implements Command {

    private final Random random = new Random();

    @Autowired
    private Waiter waiter;

    @Autowired
    private EconomyService economy;

    @Autowired
    private Translator i18n;

    @Autowired
    private ProfileManager profiles;

    @Autowired
    private Messenger msg;

    private static final int basePrize = 0;
    private static final double forSecond = 0.5;
    private static final double forCharacter = 1.2;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("scramble")
            .module(Modules.FUN)
            .permission("commands.scramble", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        String word;
        if (args.length == 0) {
            UserProfile profile = profiles.get(member);
            word = getWord(profile.getLanguage().name());
        } else {
            word = getWord(args[0]);
        }
        if (word.equals("!help")) {
            HelpArticle article = this.help(member);
            article.send(channel, member, new String[0], msg, i18n);
            return true;
        }
        msg.send(channel, "**" + member.getEffectiveName() + "**: " + i18n.get(member, "commands.fun.scramble.start", scramble(word)));
        long time = System.currentTimeMillis();
        waiter.forMessage((m) -> m.getContentRaw().contains(word), TimeUnit.SECONDS.toMillis(60)).success((m) -> {
            long seconds = 60 - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time);
            int prize = (int) ((seconds * forSecond) + (word.length() * forCharacter));
            msg.send(channel, i18n.get(member, "commands.economy.slot.win", prize));
            economy.pay(member, basePrize + prize);
        }).timeout(() -> {
            msg.send(channel, i18n.get(member, "commands.fun.scramble.fail", word));
        });
        return true;
    }

    public String scramble(String word) {
        StringBuilder res = new StringBuilder();
        for (int i = word.length() - 1; i >= 0; i--) {
            int r = 0;
            if (i != 0) {
                r = random.nextInt(i);
            }
            String c = Character.toString(word.charAt(r));
            res.append(c);
            word = word.replaceFirst(c, "");
        }
        return res.toString();
    }

    public String getWord(String lang) throws IOException {
        lang = lang.toLowerCase().replace("_", "");
        if (lang.equals("en") || lang.equals("enus")) {
            List<String> txt = Files.readAllLines(Paths.get("src\\main\\resources\\words\\en_US.txt").toAbsolutePath());
            return txt.get(random.nextInt(txt.size()));
        }
        if (lang.equals("swe")) {
            List<String> txt = Files.readAllLines(Paths.get("src\\main\\resources\\words\\swe.txt").toAbsolutePath());
            return txt.get(random.nextInt(txt.size()));
        }
        return "!help";
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("scramble")
            .text("commands.fun.scramble.description")
            .usage("L!scramble [language]")
            .permission("commands.scramble")
            .addExample("L!scramble")
            .addExample("L!scramble enUS");
        return HelpArticle.of(page);
    }

}