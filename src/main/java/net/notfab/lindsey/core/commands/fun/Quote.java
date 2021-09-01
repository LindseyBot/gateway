package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.enums.Language;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static net.notfab.lindsey.core.framework.command.Modules.FUN;

/**
 * Quote is a simple command which "generate" a random quote every time that is called.
 */
@Component
public class Quote implements Command {

    @Autowired
    private ProfileManager profiles;

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
        ServerProfile profile = profiles.get(member.getGuild());
        Language language;
        if (profile.getLanguage() == null) {
            language = Language.en_US;
        } else {
            language = profile.getLanguage();
        }
        List<String> file = getFile(language.name());
        String strJson = String.join("", file);
        HashMap<String, String> quote = this.getRandomQuote(strJson);
        this.sendEmbed(quote, channel);

        return true;
    }

    private static List<String> getFile(String language) {
        try (InputStream stream = Quote.class.getResourceAsStream("/quotes/" + language + ".json")) {
            return new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.toList());
        } catch (IOException ex) {
            return null;
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
