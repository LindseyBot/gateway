package net.notfab.lindsey.core.framework.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.notfab.lindsey.core.framework.GFXUtils;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.shared.entities.profile.server.BetterEmbedsSettings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CatarseEmbedder implements WebsiteEmbedder {

    @Autowired
    private Translator i18n;

    private final Pattern pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:catarse\\.me)/([\\w-]+)(?:[?\\w\\.=]*)$");

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
            .url("https://api.catarse.me/project_details?permalink=eq." + args)
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj;
        try {
            obj = new JSONArray(resp.body().string()).getJSONObject(0);
        } catch (Exception e) {
            return null;
        }
        if (obj.getBoolean("is_adult_content") && !nsfw) {
            return null;
        }
        embed.setTitle(obj.getString("name"), "https://www.catarse.me/" + args);
        if (!obj.isNull("headline")) {
            embed.setDescription(obj.getString("headline"));
        }
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        embed.setImage(obj.getString("original_image"));
        if (obj.getBoolean("open_for_contributions")) {
            embed.setColor(GFXUtils.YELLOW);
        } else {
            if (obj.getFloat("progress") > 99.99) {
                embed.setColor(GFXUtils.GREEN);
            } else {
                embed.setColor(GFXUtils.RED);
            }
        }
        if (!obj.isNull("headline")) {
            embed.setDescription(obj.getString("headline"));
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.mode"), obj.getString("mode"), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.category"), obj.getString("category_name"), true);
        embed.addField(i18n.get(member, "core.nsfw"), Boolean.toString(obj.getBoolean("is_adult_content")), true);
        if (!obj.isNull("pledged")) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.pledged"), "R$" + Math.round(obj.getFloat("pledged")), true);
        }
        if (!obj.isNull("progress")) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.progress"), obj.getInt("progress") + "%", true);
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.creator"), obj.getJSONObject("user").getString("public_name"), true);
        if (!obj.getJSONObject("remaining_time").isNull("total") && obj.getBoolean("open_for_contributions")) {
            if (obj.getJSONObject("remaining_time").getInt("total") != 0) {
                embed.addField(i18n.get(member, "commands.wiki.crowdfunding.remaining"), obj.getJSONObject("").getInt("total") +
                    " " + obj.getJSONObject("remaining_time").getString("unit"), true);
            } else {
                embed.addField(i18n.get(member, "commands.wiki.crowdfunding.remaining"), i18n.get(member, "commands.wiki.crowdfunding.noDate"), true);
            }
        }
        if (!obj.getJSONObject("elapsed_time").isNull("total")) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.elapsed"), obj.getJSONObject("elapsed_time").getInt("total") +
                " " + obj.getJSONObject("elapsed_time").getString("unit"), true);
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.city"), obj.getJSONObject("address").getString("city"), true);
        return embed.build();
    }

    @Override
    public boolean isEnabled(BetterEmbedsSettings settings) {
        return settings.isCatarse();
    }

}
