package net.notfab.lindsey.core.framework.i18n;

import com.moandjiezana.toml.Toml;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.framework.profile.ServerProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class Translator {

    private static final Map<Language, Toml> languageCache = new HashMap<>();

    @Autowired
    private ProfileManager profiles;

    private static InputStream getLanguage(String language) {
        return Translator.class.getResourceAsStream("/lang/" + language + ".toml");
    }

    public String get(Member member, String message, Object... args) {
        return get(member.getUser(), message, args);
    }

    public String get(User user, String message, Object... args) {
        Language language = profiles.get(user).getLanguage();
        return get(language, message, args);
    }

    public String get(Language language, String message, Object... args) {
        Toml toml;
        if (languageCache.containsKey(language)) {
            toml = languageCache.get(language);
        } else {
            toml = new Toml().parse(getLanguage(language.name()));
            languageCache.put(language, toml);
        }
        String msg = toml.getString(message);
        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return msg;
    }

    public String get(Guild guild, String message, Object... args) {
        ServerProfile profile = profiles.get(guild);
        Language language;
        if (profile.getLanguage() == null) {
            language = Language.en_US;
        } else {
            language = profile.getLanguage();
        }
        return get(language, message, args);
    }

}
