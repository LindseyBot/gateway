package net.notfab.lindsey.core.framework;

import lombok.Getter;

public enum Emotes {

    // Menu Items
    LeftArrow("141555945586163712", "325639030387769345", "LeftArrow"),
    RightArrow("141555945586163712", "325639300991418378", "RightArrow"),
    PlayButton("141555945586163712", "325671412280983552", "PlayButton"),
    StopButton("141555945586163712", "325671746793373696", "StopButton"),
    PauseButton("141555945586163712", "325682393514246145", "PauseButton"),
    SkipButton("141555945586163712", "325683016342962186", "SkipButton"),

    // User Statuses
    Online("110373943822540800", "313956277808005120", "online"),
    Offline("110373943822540800", "313956277237710868", "offline"),
    Away("110373943822540800", "313956277220802560", "away"),
    DoNotDisturb("110373943822540800", "313956276893646850", "dnd"),
    Streaming("110373943822540800", "313956277132853248", "streaming"),
    Invisible("110373943822540800", "313956277107556352", "invisible"),

    // Discord Accounts
    Partner("110373943822540800", "314068430556758017", "partner"),
    HypeSquad("110373943822540800", "314068430854684672", "hypesquad"),
    Nitro("110373943822540800", "314068430611415041", "nitro"),
    DiscordStaff("110373943822540800", "314068430787706880", "staff"),

    // Checkboxes
    Check("141555945586163712", "778417655686103080", "check"),
    XCheck("141555945586163712", "778417655476518962", "xcheck"),
    Empty("110373943822540800", "314349398723264512", "empty"),

    // Services
    Discord("110373943822540800", "314003252830011395", "discord"),
    Youtube("110373943822540800", "314349922885566475", "youtube"),
    YoutubeGaming("110373943822540800", "314349923132899338", "ytgaming"),
    SoundCloud("110373943822540800", "314349923090825216", "soundcloud"),
    Twitter("110373943822540800", "314349922877046786", "twitter"),
    Reddit("110373943822540800", "314349923103670272", "reddit"),
    Steam("110373943822540800", "314349923044687872", "steam"),
    Twitch("110373943822540800", "314349922755411970", "twitch"),

    // Others
    Shrug("141555945586163712", "332671248129720322", "shrug"),
    Awoovement("141555945586163712", "356564184722571266", "AWOOVEMENT"),
    Megumin("141555945586163712", "402450606532460554", "megumin"),
    Slot("141555945586163712", "402450801592762368", "slot", true),
    Slowmode("110373943822540800", "585790802979061760", "slowmode"),
    CopThink("141555945586163712", "775361985944485898", "copthink"),
    Crown_1("141555945586163712", "777197766362791987", "crown1"),
    Crown_2("141555945586163712", "777197766697943060", "crown2"),
    Crown_3("141555945586163712", "777197766669369346", "crown3"),

    // Emojis
    MUSIC_LOGO("notes", "\uD83C\uDFB6");

    @Getter
    private final String guild;
    @Getter
    private final String id;
    @Getter
    private final String name;
    @Getter
    private boolean animated = false;
    @Getter
    private boolean emoji = false;

    Emotes(String guild, String id, String name, boolean animated) {
        this.guild = guild;
        this.id = id;
        this.name = name;
        this.animated = animated;
    }

    Emotes(String guild, String id, String name) {
        this.guild = guild;
        this.id = id;
        this.name = name;
    }

    Emotes(String name, String utf8) {
        this.guild = null;
        this.id = utf8;
        this.name = name;
        this.emoji = true;
    }

    public String asEmote() {
        if (isEmoji()) {
            return this.id;
        } else {
            return "<" + (isAnimated() ? "a" : "") + ":" + this.getName() + ":" + this.getId() + ">";
        }
    }

    public String asReaction() {
        if (isEmoji()) {
            return this.id;
        } else {
            return this.getName() + ":" + this.getId();
        }
    }

}
