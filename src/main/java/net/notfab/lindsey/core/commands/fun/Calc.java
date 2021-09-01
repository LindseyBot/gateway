package net.notfab.lindsey.core.commands.fun;

import lombok.extern.slf4j.Slf4j;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.interaction.commands.CommandMeta;
import net.lindseybot.entities.interaction.commands.OptType;
import net.lindseybot.entities.interaction.commands.builder.CommandBuilder;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.lindsey.core.framework.command.BotCommand;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class Calc extends Command {

    private final Messenger msg;

    public Calc(Messenger msg) {
        this.msg = msg;
    }

    @Override
    public CommandMeta getMetadata() {
        return new CommandBuilder("calc", Label.raw("Calculates an expression"))
            .permission(PermissionLevel.DEVELOPER)
            .addOption(OptType.STRING, "expression", Label.raw("Mathematical expression"), true)
            .build();
    }

    @BotCommand("calc")
    public void onCommand(@NotNull ServerCommandEvent event) {
        String expr = event.getOptions().getString("expression");
        if (expr == null) {
            this.msg.reply(event, Label.raw("Missing expression"), true);
            return;
        }

        StringBuilder expression = new StringBuilder();
        if (expr.contains(",")) {
            for (String arg : expr.split(",")) {
                expression.append("\"").append(arg).append("\",");
            }
            expression = new StringBuilder(expression.substring(0, expression.length() - 1));
        } else {
            expression.append("\"").append(expr).append("\"");
        }

        OkHttpClient client = new OkHttpClient();
        String json = "{ \"expr\": [" + expression + "], \"precision\": 14}";
        Request request = new Request.Builder()
            .url("https://api.mathjs.org/v4/")
            .post(RequestBody.create(MediaType.parse("application/json"), json))
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build();
        JSONObject obj;
        try {
            Response resp = client.newCall(request).execute();
            obj = new JSONObject(resp.body().string());
        } catch (IOException ex) {
            log.error("Failed to execute math expression", ex);
            return;
        }
        if (!obj.isNull("error")) {
            this.msg.reply(event, Label.raw(obj.getString("error")), true);
            return;
        }
        StringBuilder res = new StringBuilder();
        if (expr.contains(",")) {
            String[] args = expr.split(" ");
            for (int i = 0; i < obj.getJSONArray("result").length(); i++) {
                res.append(args[i].replace(",", "")).append(" => ").append(obj.getJSONArray("result").getString(i)).append("\n");
            }
        } else {
            res.append(" => ").append(obj.getJSONArray("result").getString(0));
        }
        this.msg.reply(event, Label.raw(res.toString()), true);
    }

}
