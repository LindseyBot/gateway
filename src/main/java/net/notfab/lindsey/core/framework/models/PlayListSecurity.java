package net.notfab.lindsey.core.framework.models;

import java.util.Optional;

public enum PlayListSecurity {

    PUBLIC, SHARED, PRIVATE;

    public static Optional<PlayListSecurity> find(String arg) {
        for (PlayListSecurity item : PlayListSecurity.values()) {
            if (item.name().equalsIgnoreCase(arg)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

}
