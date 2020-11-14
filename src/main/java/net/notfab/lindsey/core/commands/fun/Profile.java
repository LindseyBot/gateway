package net.notfab.lindsey.core.commands.fun;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.framework.GFXUtils;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.inventory.InventoryService;
import net.notfab.lindsey.core.framework.inventory.Item;
import net.notfab.lindsey.core.framework.inventory.ItemMeta;
import net.notfab.lindsey.core.framework.inventory.enums.Flags;
import net.notfab.lindsey.core.framework.inventory.enums.Items;
import net.notfab.lindsey.core.framework.inventory.enums.Type;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.framework.profile.UserProfile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedString;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Profile implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private InventoryService inventory;

    @Autowired
    private ProfileManager profiles;

    @Value("${bot.token}")
    private String token;

    private final Font openSans;
    private final Font hanSansJP;
    private final Font hanSansKR;
    private final Font hanSansCN;
    private final OkHttpClient okHttpClient;

    private final ExpiringMap<Long, byte[]> userImageCache = ExpiringMap.builder()
        .expiration(5, TimeUnit.MINUTES)
        .build();

    public Profile() {
        try {
            openSans = Font.createFont(Font.TRUETYPE_FONT, this.getResource("fonts/OpenSans-Bold.ttf")).deriveFont(14f);
            hanSansJP = Font.createFont(Font.TRUETYPE_FONT, this.getResource("fonts/SourceHanSansJP-Bold.ttf")).deriveFont(14f);
            hanSansKR = Font.createFont(Font.TRUETYPE_FONT, this.getResource("fonts/SourceHanSansKR-Bold.ttf")).deriveFont(14f);
            hanSansCN = Font.createFont(Font.TRUETYPE_FONT, this.getResource("fonts/SourceHanSansCN-Bold.ttf")).deriveFont(14f);
        } catch (FontFormatException | IOException e) {
            throw new IllegalStateException("Failed to initialize fonts");
        }
        this.okHttpClient = new OkHttpClient.Builder()
            .followSslRedirects(true)
            .build();
    }

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("profile")
            .module(Modules.FUN)
            .permission("commands.profile", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        Member target = member;
        if (args.length > 0) {
            target = FinderUtil.findMember(argsToString(args, 0), message);
            if (target == null) {
                msg.send(channel, sender(member) + i18n.get(member, "search.member", argsToString(args, 0)));
                return false;
            }
        }
        try {
            InputStream stream = createProfile(target.getUser(), profiles.get(target.getUser()));
            if (stream == null) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.fun.profile.failed"));
            } else {
                channel.sendFile(stream, target.getUser().getId() + ".png").queue();
            }
        } catch (Exception e) {
            log.error("Error while loading profile (U=" + target.getUser().getId() + ")", e);
            msg.send(channel, sender(member) + i18n.get(member, "commands.fun.profile.error"));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("profile")
            .text("commands.fun.profile.description")
            .usage("L!profile [member]")
            .url("https://github.com/LindseyBot/core/wiki/commands-profile")
            .permission("commands.profile")
            .addExample("L!profile")
            .addExample("L!profile @lindsey");
        return HelpArticle.of(page);
    }

    private InputStream getResource(String name) {
        return getClass().getResourceAsStream("/assets/templates/" + name);
    }

    private InputStream createProfile(User user, UserProfile profile) throws Exception {
        BufferedImage template = ImageIO.read(this.getResource("profile.png"));
        Graphics2D templateGraphics = template.createGraphics();
        Color fontColor = Color.LIGHT_GRAY;

        templateGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        templateGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        templateGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // -- Add Header
        {
            Items background = profile.getBackground();
            if (background != null) {
                ItemMeta meta = background.getMetadata();
                if (meta != null && meta.getFontColor() != null) {
                    fontColor = GFXUtils.getColor(meta.getFontColor());
                }
                BufferedImage bufferedImage = background.getImage();
                if (bufferedImage != null) {
                    templateGraphics.drawImage(bufferedImage, 0, 0, null);
                }
            }
        }

        // -- Add profile picture
        {
            InputStream profileStream = downloadImage(user);
            if (profileStream == null) return null;
            BufferedImage picture = ImageIO.read(profileStream);
            picture = GFXUtils.resize(picture, 128, 128);
            templateGraphics.drawImage(picture, 24, 39, null);
        }

        // -- Add account ribbon
        {
            BufferedImage image = null;
            if (profile.getOwner() == 87166524837613568L) {
                image = ImageIO.read(this.getResource("ribbons/developer.png"));
            } else if (profile.getOwner() == 119566649731842049L) {
                image = ImageIO.read(this.getResource("ribbons/designer.png"));
            } else if (profile.getOwner() == 119482224713269248L) {
                image = ImageIO.read(this.getResource("ribbons/official.png"));
            }
            if (image != null) {
                templateGraphics.drawImage(image, 0, 128, null);
            }
        }

        // -- Add country flag
        {
            Flags country = profile.getCountry();
            if (country == null) {
                country = Flags.Unknown;
            }
            BufferedImage flag = country.getImage();
            templateGraphics.drawImage(flag, 168, 39, null);
        }

        /* Badges
         * 1: 32, 223
         * 2: 104, 223
         * 3: 176, 223
         * 4: 248, 223
         * 5: 320, 223
         * 6: 392, 223
         * 7: 464, 223
         * 8: 536, 223
         */
        {
            List<Items> badges = inventory.findAllByType(profile.getOwner(), Type.BADGE)
                .stream()
                .map(Item::getModel)
                .collect(Collectors.toList());
            for (int i = 0; i < (Math.min(badges.size(), 8)); i++) {
                Items badge = badges.get(i);
                templateGraphics.drawImage(badge.getImage(), 32 + (72 * i), 223, null);
            }
        }

        // -- Prepare Write Tools
        templateGraphics.setColor(fontColor);
        templateGraphics.setFont(openSans);

        // -- Write UserName + Discriminator
        templateGraphics.drawString(getUTF8(user.getAsTag()).getIterator(), 223, 59);
        templateGraphics.setColor(Color.LIGHT_GRAY);

        // -- Write Cookie Count
        templateGraphics.drawString(profile.getCookies() + "", 96, 343);

        // -- Write Cookies
        templateGraphics.drawString("Cookies", 108, 318);

        // -- Cleanup and return
        templateGraphics.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(template, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    private AttributedString getUTF8(String text) {
        AttributedString result = new AttributedString(text);
        for (int i = 0; i < text.length(); i++) {
            char entry = text.charAt(i);
            if (openSans.canDisplay(entry)) {
                continue;
            }
            try {
                if (hanSansKR.canDisplay(entry)) {
                    result.addAttribute(TextAttribute.FONT, hanSansKR, i, i);
                } else if (hanSansJP.canDisplay(entry)) {
                    result.addAttribute(TextAttribute.FONT, hanSansJP, i, i);
                } else if (hanSansCN.canDisplay(entry)) {
                    result.addAttribute(TextAttribute.FONT, hanSansCN, i, i);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    private InputStream downloadImage(User user) {
        if (this.userImageCache.containsKey(user.getIdLong())) {
            return Utils.toStream(this.userImageCache.get(user.getIdLong()));
        }
        String url = user.getEffectiveAvatarUrl() + "?size=128";
        try {
            Request request = new Request.Builder().url(url).addHeader("Authorization", "Bot " + token).get().build();
            Response resp = okHttpClient.newCall(request).execute();
            if (!resp.isSuccessful()) {
                resp.close();
                return null;
            }
            ResponseBody body = resp.body();
            if (body == null) {
                resp.close();
                return null;
            }
            userImageCache.put(user.getIdLong(), body.bytes());
            resp.close();
            return Utils.toStream(userImageCache.get(user.getIdLong()));
        } catch (IOException e) {
            return null;
        }
    }

}
