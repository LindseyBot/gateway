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

import java.awt.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PicartoEmbedder implements WebsiteEmbedder {

    @Autowired
    private Translator i18n;

    private final Pattern pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:picarto\\.tv)/([\\w-]+)(?:[?\\w\\.=]*)$");

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
        JSONObject obj;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url("https://api.picarto.tv/v1/channel/name/" + args)
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        try {
            obj = new JSONObject(resp.body().string());
        } catch (Exception e) {
            return null;
        }
        boolean adult = obj.getBoolean("adult");
        if (adult && !nsfw) {
            return null;
        }
        EmbedBuilder embed = new EmbedBuilder();
        String title = obj.getString("name");
        embed.setThumbnail(obj.getString("avatar"));
        if (obj.getBoolean("online")) {
            title = title + " - Online";
            embed.addField(i18n.get(member, "commands.lives.title"), obj.getString("title"), true);
            embed.addField(i18n.get(member, "commands.lives.category"), obj.getString("category"), true);
            embed.addField("NSFW", Boolean.toString(nsfw), true);
            embed.addField(i18n.get(member, "commands.lives.viewers"), String.valueOf(obj.getInt("viewers")), true);
            embed.setImage(obj.getJSONObject("thumbnails").getString("web"));
            embed.setColor(Color.red);
        } else {
            embed.addField(i18n.get(member, "commands.lives.last"), obj.getString("last_live").substring(0, 9), true);
            embed.addField(i18n.get(member, "commands.lives.category"), obj.getString("category"), true);
            embed.addField("NSFW", String.valueOf(obj.getBoolean("adult")), true);
            embed.addField(i18n.get(member, "commands.lives.total"), String.valueOf(obj.getInt("viewers_total")), true);
            embed.setColor(Color.gray);
        }
        embed.setTitle(title, "https://picarto.tv/" + obj.getString("name"));
        embed.addField(i18n.get(member, "commands.lives.followers"), String.valueOf(obj.getInt("followers")), true);
        embed.addField(i18n.get(member, "commands.lives.language"), obj.getJSONArray("languages").getJSONObject(0).getString("name"), true);
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        return embed.build();
    }

}