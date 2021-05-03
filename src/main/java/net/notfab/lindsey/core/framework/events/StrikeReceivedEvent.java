package net.notfab.lindsey.core.framework.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.notfab.eventti.Event;
import net.notfab.lindsey.shared.entities.profile.member.Strike;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrikeReceivedEvent extends Event {

    private Strike strike;
    private int total;

}
