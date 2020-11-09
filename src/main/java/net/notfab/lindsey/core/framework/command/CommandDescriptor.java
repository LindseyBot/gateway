package net.notfab.lindsey.core.framework.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.notfab.lindsey.core.framework.permissions.Permission;

import java.util.*;

@Data
@AllArgsConstructor
public class CommandDescriptor {

    private final List<String> aliases;
    private final Set<Permission> permissions;

    private String name;
    private Modules module;

    public static class Builder {

        private final List<String> aliases = new ArrayList<>();
        private final Set<Permission> permissions = new HashSet<>();

        private String name;
        private Modules module;

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
            this.permissions.add(new Permission(name, description, true));
            return this;
        }

        public Builder permission(String name, String description, boolean allowed) {
            this.permissions.add(new Permission(name, description, allowed));
            return this;
        }

        public CommandDescriptor build() {
            return new CommandDescriptor(aliases, permissions, name, module);
        }

    }

}
