package net.notfab.lindsey.framework.command;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class CommandDescriptor {

    private String name;
    private List<String> aliases;

    public static class Builder {

        private String name;
        private Modules module;
        private List<String> aliases = new ArrayList<>();
        private Map<String, String> permissions = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder module(Modules module) {
            this.module = module;
            return this;
        }

        public Builder alias(String... names) {
            Collections.addAll(this.aliases, names);
            return this;
        }

        public Builder permission(String name, String description) {
            this.permissions.put(name.toLowerCase(), description);
            return this;
        }

        public CommandDescriptor build() {
            return new CommandDescriptor(name, aliases);
        }

    }

}
