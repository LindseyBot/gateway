package net.notfab.lindsey.framework.actions.impl.triggers;

import lombok.Data;
import net.notfab.lindsey.framework.actions.Trigger;

@Data
public class ReactionAddTrigger implements Trigger {

    private String messageId;
    private String emoteId;

}
