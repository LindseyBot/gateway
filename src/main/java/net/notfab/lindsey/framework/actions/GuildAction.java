package net.notfab.lindsey.framework.actions;

import lombok.Data;

import java.util.List;

@Data
public class GuildAction {

    private Trigger trigger;

    private Condition condition;

    private List<Action> actions;

}
