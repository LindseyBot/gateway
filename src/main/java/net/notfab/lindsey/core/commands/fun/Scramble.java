package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.economy.EconomyService;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.framework.waiter.Waiter;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.enums.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class Scramble implements Command {

    private final Random random = new Random();

    private List<Long> active = new ArrayList<>();

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
        long id = channel.getGuild().getIdLong();
        if (active.contains(id)) {
            msg.send(channel, i18n.get(member, "commands.fun.scramble.active"));
            return true;
        }
        String word;
        if (args.length == 0) {
            ServerProfile profile = profiles.get(member.getGuild());
            Language language;
            if (profile.getLanguage() == null) {
                language = Language.en_US;
            } else {
                language = profile.getLanguage();
            }
            word = getWord(language.name());
        } else {
            word = getWord(args[0]);
        }
        if (word.equals("!help")) {
            HelpArticle article = this.help(member);
            article.send(channel, member, new String[0], msg, i18n);
            return true;
        }
        active.add(channel.getGuild().getIdLong());
        msg.send(channel, "**" + member.getEffectiveName() + "**: " + i18n.get(member, "commands.fun.scramble.start", scramble(word)));
        long time = System.currentTimeMillis();
        waiter.forMessage((m) -> m.getContentRaw().equalsIgnoreCase(word) && m.getTextChannel().equals(channel), TimeUnit.SECONDS.toMillis(60)).success((m) -> {
            long seconds = 60 - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time);
            int prize = (int) ((seconds * forSecond) + (word.length() * forCharacter));
            active.remove(id);
            msg.send(channel, "**" + member.getEffectiveName() + "**: " + i18n.get(member, "commands.economy.win", prize));
            economy.pay(member, basePrize + prize);
        }).timeout(() -> {
            active.remove(id);
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

    public String getWord(String lang) {
        String language = "";
        switch (lang.toLowerCase().replace("_", "")) {
            case "en", "enus" -> language = "en_US";
            case "swe" -> language = "swe";
        }
        if (!language.equals("")) {
            List<String> file = getFile(language);
            return file.get(random.nextInt(file.size()));
        }
        return "!help";
    }

    private static List<String> getFile(String language) {
        try (InputStream stream = Scramble.class.getResourceAsStream("/words/" + language + ".txt")) {
            return new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.toList());
        } catch (IOException ex) {
            return null;
        }
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
