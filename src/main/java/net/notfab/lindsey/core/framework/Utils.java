package net.notfab.lindsey.core.framework;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.extractors.SCExtractor;
import net.notfab.lindsey.core.framework.extractors.YTExtractor;
import net.notfab.lindsey.core.framework.i18n.Translator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<>(
                list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    public static String getTime(long millis, Member member, Translator i18n) {
        return getTime(millis, member.getUser(), i18n);
    }

    /**
     * @param millis - Time in milliseconds
     * @return String with time (1 day 3 hours 1 minute 10 seconds)
     */
    private static String getTime(Long millis, User user, Translator i18n) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        StringBuilder time = new StringBuilder();
        if (days > 0) {
            time.append(" ");
            if (days > 1) {
                time.append(i18n.get(user, "parts.days", String.valueOf(days)));
            } else {
                time.append(i18n.get(user, "parts.day", String.valueOf(days)));
            }
        }
        if (hours > 0) {
            time.append(" ");
            if (hours > 1) {
                time.append(i18n.get(user, "parts.hours", String.valueOf(hours)));
            } else {
                time.append(i18n.get(user, "parts.hour", String.valueOf(hours)));
            }
        }
        if (minutes > 0) {
            time.append(" ");
            if (minutes > 1) {
                time.append(i18n.get(user, "parts.minutes", String.valueOf(minutes)));
            } else {
                time.append(i18n.get(user, "parts.minute", String.valueOf(minutes)));
            }
        }
        if (seconds > 0) {
            time.append(" ");
            if (seconds > 1) {
                time.append(i18n.get(user, "parts.seconds", String.valueOf(seconds)));
            } else {
                time.append(i18n.get(user, "parts.second", String.valueOf(seconds)));
            }
        }
        return time.substring(1, time.length());
    }

    public static <T> Consumer<T> noop() {
        return t -> {
        };
    }

    public static InputStream cloneStream(InputStream inputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException ex) {
            return null;
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static InputStream toStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static boolean isURL(String string) {
        try {
            new URL(string);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    public static boolean isNumber(String arg) {
        return isLong(arg) || isDouble(arg);
    }

    public static boolean isInt(String arg) {
        try {
            if (arg == null) return false;
            Integer.parseInt(arg);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isLong(String arg) {
        try {
            if (arg == null) return false;
            Long.parseLong(arg);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isDouble(String arg) {
        try {
            if (arg == null) return false;
            Double.parseDouble(arg);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static Optional<Integer> safeInt(String msg) {
        try {
            return Optional.of(Integer.parseInt(msg));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public static boolean isSupportedMusicURL(String nameOrURL) {
        YTExtractor youtube = new YTExtractor();
        SCExtractor soundCloud = new SCExtractor();
        return youtube.isSupported(nameOrURL) || soundCloud.isSupported(nameOrURL);
    }

    public static Optional<Boolean> parseBoolean(String argument) {
        return switch (argument.toLowerCase()) {
            case "true", "t", "yes", "y", "enable", "e", "enabled", "1", "allow" -> Optional.of(true);
            case "false", "f", "no", "n", "disable", "d", "disabled", "0", "deny" -> Optional.of(false);
            default -> Optional.empty();
        };
    }

    public static boolean isImgur(String url) {
        Pattern pattern = Pattern.compile("i\\.imgur\\.com/(\\w+)\\.(\\w+)$");
        Matcher matcher = pattern.matcher(url);
        return matcher.find();
    }

    public static boolean isDiscordModerator(Member member) {
        return member.isOwner()
            || member.hasPermission(Permission.MANAGE_PERMISSIONS)
            || member.hasPermission(Permission.KICK_MEMBERS)
            || member.hasPermission(Permission.BAN_MEMBERS)
            || member.hasPermission(Permission.MANAGE_SERVER)
            || member.hasPermission(Permission.MESSAGE_MANAGE)
            || member.hasPermission(Permission.ADMINISTRATOR);
    }

}
