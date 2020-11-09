package net.notfab.lindsey.core.framework.command.help;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.notfab.lindsey.core.framework.i18n.Translator;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HelpPage {

    private final String name;
    private final List<String> examples = new ArrayList<>();
    private String text;
    private String syntax;
    private String permission;
    private String url;

    public HelpPage(String name) {
        this.name = name;
    }

    public HelpPage text(String text) {
        this.text = text;
        return this;
    }

    public HelpPage url(String url) {
        this.url = url;
        return this;
    }

    public HelpPage addExample(String example) {
        this.examples.add(example);
        return this;
    }

    public HelpPage usage(String syntax) {
        this.syntax = syntax;
        return this;
    }

    public HelpPage permission(String permission) {
        this.permission = permission;
        return this;
    }

    public MessageEmbed asEmbed(Translator i18n, Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(i18n.get(member, "help.title", this.getName()));

        StringBuilder text = new StringBuilder();
        text.append(i18n.get(member, this.getText()));
        text.append("\n\n");
        if (this.getUrl() != null) {
            text.append(i18n.get(member, "help.url", "<" + this.getUrl() + ">"));
            text.append("\n");
        }
        if (this.getPermission() != null) {
            text.append(i18n.get(member, "help.permission", this.getPermission()));
            text.append("\n");
        }
        if (this.getSyntax() != null) {
            text.append("\n");
            text.append(i18n.get(member, "help.syntax"));
            text.append("\n");
            text.append("```").append(this.getSyntax()).append("```");
            text.append("\n");
        }
        if (!this.getExamples().isEmpty()) {
            text.append(i18n.get(member, "help.examples"));
            text.append("\n```");
            this.getExamples().forEach(x -> text.append("\n").append(x));
            text.append("\n```");
        }
        builder.setDescription(text.toString());

        builder.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
        return builder.build();
    }

}
