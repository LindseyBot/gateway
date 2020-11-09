package net.notfab.lindsey.core.commands.economy;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.Emotes;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.economy.EconomyService;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.leaderboard.LeaderboardService;
import net.notfab.lindsey.core.framework.leaderboard.LeaderboardType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class Slot implements Command {

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

    private final Random random = new Random();
    private static final double multiplier = 1.0;
    private static final int price = 10;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("slot")
            .alias("slotmachine")
            .module(Modules.ECONOMY)
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

        String txt = i18n.get(member, "commands.economy.slot.start", member.getEffectiveName()) + "\n" +
            i18n.get(member, "commands.economy.slot.values", price, getJackpot()) + "\n" +
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
        m.editMessage(m.getContentRaw().replaceFirst(Emotes.Slot.asEmote(), getEmote(c))).complete();

        TimeUnit.SECONDS.sleep(1);
        int win;
        int prize = getPrize(a, b, c);
        if (prize == -5) {
            ldService.update(member, LeaderboardType.SLOT_WINS);
            win = getJackpot();
            redis.opsForValue().set("Lindsey:SlotJackpot", "0");
        } else if (prize == 0) {
            msg.send(channel, i18n.get(member, "commands.economy.slot.lost"));
            return true;
        } else {
            redis.opsForValue().increment("Lindsey:SlotJackpot", (price / 2));
            win = (int) (prize * multiplier);
        }
        economy.pay(member, win);
        msg.send(channel, i18n.get(member, "commands.economy.slot.win", win));
        return true;
    }

    public int getPrize(int a, int b, int c) {
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
        String emote = ":octagonal_sign:";
        emote = switch (value) {
            case 0 -> Emotes.Megumin.asEmote();
            case 7, 8 -> ":fire:";
            case 5, 6 -> ":eyes:";
            case 4, 3 -> ":eggplant:";
            case 2, 1 -> Emotes.Shrug.asEmote();
            default -> emote;
        };
        return emote;
    }

    public int getJackpot() {
        String jack = redis.opsForValue().get("Lindsey:SlotJackpot");
        if (jack == null) {
            return 0;
        } else {
            return Integer.parseInt(jack);
        }
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("slot")
            .text("commands.economy.slot.description")
            .usage("L!slot")
            .permission("commands.slot")
            .addExample("L!slot");
        return HelpArticle.of(page);
    }

}
