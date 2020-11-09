package net.notfab.lindsey.core.framework.profile.member;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class RoleHistory {

    private long lastUpdated;
    private Set<String> roles = new HashSet<>();

}
