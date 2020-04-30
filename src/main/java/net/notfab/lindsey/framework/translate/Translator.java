package net.notfab.lindsey.framework.translate;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.io.IOException;

public class Translator {

    public static String translate(String language, String message) throws IOException {
        Toml toml = new Toml().parse(file(language));
        toml.getString(message);
        return toml.getString(message);
    }

    private static File file(String language) {
        File f = null;
        if (language.equals("en")) {
            f = new File("C:\\Users\\Felipe Schneider\\IdeaProjects\\LindseyBot\\core\\src\\main\\java\\net\\notfab\\lindsey\\framework\\translate\\files\\en.toml");
        }
        return f;
    }

}
