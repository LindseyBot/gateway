package net.notfab.lindsey.core.framework.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.notfab.lindsey.core.framework.GFXUtils;
import net.notfab.lindsey.core.framework.i18n.Translator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApoiaseEmbedder implements WebsiteEmbedder {

    @Autowired
    private Translator i18n;

    private final Pattern pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:apoia\\.se)/([\\w-]+)(?:[?\\w\\.=]*)$");

    @Override
    public boolean isSupported(String url) {
        return pattern.matcher(url).find();
    }

    @Override
    public MessageEmbed getEmbed(String url, Member member, boolean nsfw) throws IOException {
        Matcher matcher = this.pattern.matcher(url);
        if (!matcher.find()) {
            return null;
        }
        String args = matcher.group(1);
        OkHttpClient client = new OkHttpClient();
        EmbedBuilder embed = new EmbedBuilder();
        Request request = new Request.Builder()
            .url("https://apoia.se/api/v1/users/" + args)
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj = new JSONObject(resp.body().string());
        JSONObject campaigns = obj.getJSONArray("campaigns").getJSONObject(0);
        JSONObject state = obj.getJSONArray("address").getJSONObject(0);
        if (campaigns.getBoolean("explicit") && !nsfw) {
            return null;
        }
        embed.setTitle(campaigns.getString("name"), "https://www.apoia.se/" + campaigns.getString("slug"));
        if (!campaigns.getJSONObject("about").isNull("slogan")) {
            embed.setDescription(campaigns.getJSONObject("about").getString("slogan"));
        }
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        embed.setImage(campaigns.getJSONObject("about").getString("photo"));
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.category"), campaigns.getJSONArray("categories").getString(0), true);
        if (!campaigns.getJSONArray("goals").isEmpty()) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.goal"), "R$" + campaigns.getJSONArray("goals").getJSONObject(0).getInt("value"), true);
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.creator"), obj.getString("username"), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.createdDate"), campaigns.getString("createdDate").split("T")[0], true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.city"), state.getString("city") + " - " + state.getString("state"), true);
        embed.addField(i18n.get(member, "core.nsfw"), Boolean.toString(nsfw), true);
        if (campaigns.getString("status").equals("published")) {
            embed.setColor(GFXUtils.GREEN);
        } else {
            embed.setColor(GFXUtils.RED);
        }
        return embed.build();
    }

}
