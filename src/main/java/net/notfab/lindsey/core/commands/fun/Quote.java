package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.i18n.Messenger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import static net.notfab.lindsey.framework.command.Modules.FUN;

/**
 * Quote is a simple command which "generate" a random quote every time that is called.
 */
@Component
public class Quote implements Command {

    private static final String QUOTES_URL = "https://type.fit/api/quotes";
    private static final OkHttpClient client = new OkHttpClient().newBuilder()
        .followSslRedirects(true)
        .build();

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("quote")
            .module(FUN)
            .permission("commands.quote", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) {
        try {
            Request request = new Request.Builder()
                .url(QUOTES_URL)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();

            Response response = client.newCall(request).execute();
            String strJson = response.body().string();
            HashMap<String, String> quote = this.getRandomQuote(strJson);
            this.sendEmbed(quote, channel);

            return true;
        } catch (IOException e) {
            this.handleError(channel);
            return false;
        }
    }

    /**
     * Build quotes array and select randomly a quote
     *
     * @param json Json on string format that will be parsed into an array in order to select the quote
     * @return A HashMap containing the quote info, like it's author and the text(phrase)
     */
    private HashMap<String, String> getRandomQuote(String json) {
        JSONArray jsonArray = new JSONArray(json);

        Random rand = new Random();
        int randomIndex = rand.nextInt(jsonArray.length());
        JSONObject phrase = jsonArray.getJSONObject(randomIndex);

        HashMap<String, String> quote = new HashMap<>();
        quote.put("text", phrase.getString("text"));
        quote.put("author", phrase.getString("author"));

        return quote;
    }

    /**
     * Method responsible to build embed that will show the selected random quote.
     *
     * @param quote   Quote HashMap object that will be used to build embed
     * @param channel Channel that the embed will be sent
     */
    private void sendEmbed(HashMap<String, String> quote, TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(quote.get("text"));
        embed.setDescription(quote.get("author"));

        msg.send(channel, embed.build());
    }

    /**
     * Send a generic message on channel in case of any errors on Command, like API offline.
     *
     * @param channel Channel that message will be sent
     */
    private void handleError(TextChannel channel) {
        channel.sendMessage("Today's defeat will be greater tomorrow, don't worry.").queue();
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("quote")
            .text("commands.fun.quote.description")
            .usage("L!quote")
            .permission("commands.quote")
            .addExample("L!quote");
        return HelpArticle.of(page);
    }

}
