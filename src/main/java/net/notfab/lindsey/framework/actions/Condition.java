package net.notfab.lindsey.framework.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.actions.impl.conditions.GroupAnd;
import net.notfab.lindsey.framework.actions.impl.conditions.GroupOr;
import net.notfab.lindsey.framework.actions.impl.conditions.MessageContains;
import net.notfab.lindsey.framework.actions.impl.conditions.NoOpCondition;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NoOpCondition.class, name = "NoOpCondition"),
    @JsonSubTypes.Type(value = GroupAnd.class, name = "GroupAnd"),
    @JsonSubTypes.Type(value = GroupOr.class, name = "GroupOr"),
    @JsonSubTypes.Type(value = MessageContains.class, name = "MessageContains"),
})
public interface Condition {

    boolean is(Guild guild, Member member, TextChannel channel, Message message);

}
