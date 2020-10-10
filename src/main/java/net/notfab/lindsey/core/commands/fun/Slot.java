package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.Emotes;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.economy.EconomyService;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import net.notfab.lindsey.framework.leaderboard.LeaderboardService;
import net.notfab.lindsey.framework.leaderboard.LeaderboardType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class Slot implements Command {

    private final Random random = new Random();

    @Autowired
    private Translator i18n;

    @Autowired
    private LeaderboardService ldService;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private Messenger msg;

    @Autowired
    private EconomyService economy;

    private final double mult = 1.0;
    private final int price = 10;
    private int jackpot = 0;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("slot")
            .alias("slotmachine")
            .module(Modules.FUN)
            .permission("commands.slot", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {

        if (!economy.has(member, price)) {
            msg.send(channel, i18n.get(member, "commands.economy.not_enough"));
            return true;
        }
        economy.deduct(member, price);

        try {
            jackpot = Integer.parseInt(redis.opsForValue().get("lindsey:slotJackpot"));
        } catch (Exception e) {
            jackpot = 0;
        }

        String txt = member.getEffectiveName() + " " + i18n.get(member, "commands.fun.slot.start") + "\n" +
            i18n.get(member, "commands.fun.slot.values", price, jackpot) + "\n" +
            Emotes.Slot.asEmote() + " | " + Emotes.Slot.asEmote() + " | " + Emotes.Slot.asEmote() + "\n \n";
        Message m = channel.sendMessage(txt).complete();

        TimeUnit.SECONDS.sleep(3);
        int a = random.nextInt(8);

        m = m.editMessage(m.getContentRaw().replaceFirst(Emotes.Slot.asEmote(), getEmote(a))).complete();

        TimeUnit.SECONDS.sleep(1);
        int b = random.nextInt(8);

        m = m.editMessage(m.getContentRaw().replaceFirst(Emotes.Slot.asEmote(), getEmote(b))).complete();

        TimeUnit.SECONDS.sleep(1);
        int c = random.nextInt(8);

        m = m.editMessage(m.getContentRaw().replaceFirst(Emotes.Slot.asEmote(), getEmote(c))).complete();

        TimeUnit.SECONDS.sleep(1);

        int win = 0;
        if (getPrize(a, b, c) == -5) {
            ldService.update(member, LeaderboardType.SLOT_WINS);
            win = jackpot;

            redis.opsForValue().set("lindsey:slotJackpot", "0");

        } else {

            redis.opsForValue().increment("lindsey:slotJackpot", (long) (price / 2));

            win = (int) (getPrize(a, b, c) * mult);
        }

        economy.pay(member, win);

        msg.send(channel, i18n.get(member, "commands.fun.slot.win", win));

        return true;
    }

    public double getPrize(int a, int b, int c) {
        int prize;
        if (a == b && b == c) {
            prize = 20;
            prize = switch (a) {
                case 0 -> -5;
                case 7, 8 -> 100;
                default -> prize;
            };
            return prize;
        }
        if (a == b || b == c || a == c) {
            prize = 10;
            prize = switch (a) {
                case 0 -> 50;
                case 7, 8 -> 30;
                default -> prize;
            };
            return prize;
        }
        return 0;
    }

    public String getEmote(int value) {
        String r = ":octagonal_sign:";
        r = switch (value) {
            case 0 -> Emotes.Megumin.asEmote();
            case 7, 8 -> ":fire:";
            case 5, 6 -> ":eyes:";
            case 4, 3 -> ":eggplant:";
            case 2, 1 -> Emotes.Shrug.asEmote();
            default -> r;
        };
        return r;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("slot")
            .text("commands.fun.slot.description")
            .usage("L!slot")
            .permission("commands.slot")
            .addExample("L!slot");
        return HelpArticle.of(page);
    }

}