package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.jodah.expiringmap.ExpiringMap;
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
import net.notfab.lindsey.core.framework.models.BlackjackModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Blackjack implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private EconomyService economy;

    @Autowired
    private ExpiringMap<Long, BlackjackModel> cache;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("blackjack")
            .alias("21")
            .module(Modules.FUN)
            .permission("commands.blackjack", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        int price;
        if (args.length > 0 && Character.isDigit(args[0].charAt(0))) {
            price = Integer.parseInt(args[0]);
        } else {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }
        if (Integer.parseInt(args[0]) < 1 || Integer.parseInt(args[0]) > 5000) {
            msg.send(channel, i18n.get(member, "commands.fun.blackjack.price"));
            return false;
        }
        if (!economy.has(member, price)) {
            msg.send(channel, i18n.get(member, "commands.economy.not_enough"));
            return true;
        }
        economy.deduct(member, price);

        BlackjackModel model = new BlackjackModel();
        model.setId(member.getIdLong());
        model.setPrice(price);
        model.start();

        String txt = model.getMessage(member);
        channel.sendMessage(txt).queue(msg -> {
            cache.put(msg.getIdLong(), model);
            msg.addReaction(Emotes.Check.asReaction()).queue();
            msg.addReaction(Emotes.XCheck.asReaction()).queue();
        });

        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("blackjack")
            .text("commands.fun.blackjack.description")
            .usage("L!blackjack <bet>")
            .permission("commands.blackjack")
            .addExample("L!blackjack 10")
            .addExample("L!21 100");
        return HelpArticle.of(page);
    }

}
