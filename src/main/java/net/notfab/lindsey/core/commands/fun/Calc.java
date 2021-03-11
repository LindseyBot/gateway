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
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Calc implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("calc")
            .module(Modules.FUN)
            .permission("commands.calc", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }

        StringBuilder expression = new StringBuilder();
        if (this.argsToString(args, 0).contains(",")) {
            for (String arg : this.argsToString(args, 0).split(",")) {
                expression.append("\"").append(arg).append("\",");
            }
            expression = new StringBuilder(expression.substring(0, expression.length() - 1));
        } else {
            expression.append("\"").append(this.argsToString(args, 0)).append("\"");
        }

        OkHttpClient client = new OkHttpClient();
        String json = "{ \"expr\": [" + expression + "], \"precision\": 14}";
        Request request = new Request.Builder()
            .url("https://api.mathjs.org/v4/")
            .post(RequestBody.create(MediaType.parse("application/json"), json))
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj = new JSONObject(resp.body().string());
        if (!obj.isNull("error")) {
            msg.send(channel, obj.getString("error"));
            return false;
        }

        StringBuilder res = new StringBuilder();
        if (this.argsToString(args, 0).contains(",")) {
            for (int i = 0; i < obj.getJSONArray("result").length(); i++) {
                res.append(args[i].replace(",", "")).append(" => ").append(obj.getJSONArray("result").getString(i)).append("\n");
            }
        } else {
            res.append(" => ").append(obj.getJSONArray("result").getString(0));
        }
        msg.send(channel, res.toString());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("calc")
            .text("commands.fun.calc.description")
            .usage("L!calc <expression>")
            .permission("commands.calc")
            .addExample("L!calc 2+3")
            .addExample("L!calc x=2+3, y=2+x")
            .addExample("L!calc 12 inch to cm");
        return HelpArticle.of(page);
    }

}