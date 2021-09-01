package net.notfab.lindsey.core.commands.wiki;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Hearthstone implements Command {

    @Value("${bot.integrations.rapidapi}")
    private String key;

    @Autowired
    private ProfileManager profiles;

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("hearthstone")
            .alias("hscard")
            .module(Modules.GAME_WIKI)
            .permission("commands.hearthstone", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }

        UserProfile profile = profiles.getUser(member);
        OkHttpClient client = new OkHttpClient();
        String cardname = "";
        String locale = profile.getLanguage().name().replaceAll("_", "");
        boolean gold = false;

        if (args.length == 1) {  //Available locales: enUS, enGB, deDE, esES, esMX, frFR, itIT, koKR, plPL, ptBR, ruRU, zhCN, zhTW, jaJP, thTH.
            cardname = args[0];
        } else if (args.length == 2) {
            cardname = args[0];
            if (args[1].equals("true")) {
                gold = true;
            }
        } else if (args.length == 3) {
            cardname = args[0];
            locale = args[2];
            if (args[1].equals("true")) {
                gold = true;
            }
        }

        Request request = new Request.Builder()
            .url("https://omgvamp-hearthstone-v1.p.rapidapi.com/cards/search/" + cardname + "?collectible=1&locale=" + locale)
            .get()
            .addHeader("x-rapidapi-host", "omgvamp-hearthstone-v1.p.rapidapi.com")
            .addHeader("x-rapidapi-key", key)
            .build();
        Response resp = client.newCall(request).execute();
        String str = resp.body().string();

        String result = "";
        try {
            JSONArray arr = new JSONArray(str);
            if (gold) {
                result = arr.getJSONObject(0).getString("imgGold");
            } else {
                result = arr.getJSONObject(0).getString("img");
            }
        } catch (JSONException e) {
            JSONObject obj = new JSONObject(str);
            if (obj.getInt("error") == 404) {
                result = i18n.get(member, "commands.wiki.hearthstone.404");
            }
        }

        msg.send(channel, result);

        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("hearthstone")
            .text("commands.wiki.hearthstone.description")
            .usage("L!hscard <cardName> [isGold] [language]")
            .permission("commands.hearthstone")
            .addExample("L!hearthstone \"Leeroy Jenkins\"")
            .addExample("L!hscard Leeroy true")
            .addExample("L!hscard Leeroy true ptBR");
        return HelpArticle.of(page);
    }

}
