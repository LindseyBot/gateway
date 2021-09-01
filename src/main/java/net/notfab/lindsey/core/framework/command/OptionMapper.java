package net.notfab.lindsey.core.framework.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public record OptionMapper(List<OptionMapping> options) {

    public boolean has(String name) {
        return this.options.stream()
            .anyMatch(o -> o.getName().equals(name));
    }

    private OptionMapping get(String name) {
        return this.options.stream()
            .filter(o -> o.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public Member getMember(String name) {
        if (!this.has(name)) {
            return null;
        } else {
            return this.get(name).getAsMember();
        }
    }

    public User getUser(String name) {
        if (!this.has(name)) {
            return null;
        } else {
            return this.get(name).getAsUser();
        }
    }

    public int getInt(String name) {
        if (!this.has(name)) {
            return 0;
        } else {
            return (int) this.get(name).getAsLong();
        }
    }

    public String getString(String name) {
        if (!this.has(name)) {
            return null;
        } else {
            return this.get(name).getAsString();
        }
    }

    public long getLong(String name) {
        if (!this.has(name)) {
            return 0L;
        } else {
            return this.get(name).getAsLong();
        }
    }

}
