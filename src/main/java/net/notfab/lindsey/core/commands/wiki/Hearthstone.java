package net.notfab.lindsey.core.commands.wiki;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import net.notfab.lindsey.framework.settings.ProfileManager;
import net.notfab.lindsey.framework.settings.UserProfile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Hearthstone implements Command {

    @Autowired
    private ProfileManager profiles;

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("hscard")
                .module(Modules.GAME_WIKI)
                .permission("commands.hearthstone", "Permission to use the base command")
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {

        UserProfile profile = profiles.get(member);

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
                .url("https://omgvamp-hearthstone-v1.p.rapidapi.com/cards/" + cardname + "?locale=" + locale + "&collectible=1")
                .get()
                .addHeader("x-rapidapi-host", "omgvamp-hearthstone-v1.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "79abb4b7fbmshb0ff1c23e6aab71p142b50jsn5730c96920f2")
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
        }
        catch (JSONException e){
            JSONObject obj = new JSONObject(str);
            if(obj.getInt("error") == 404){
                result = i18n.get(member, "commands.wiki.hearthstone.404");
            }
        }

        msg.send(channel, result);
        return false;
    }
}
