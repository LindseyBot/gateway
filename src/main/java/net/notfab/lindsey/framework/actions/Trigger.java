package net.notfab.lindsey.framework.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.notfab.lindsey.framework.actions.impl.triggers.MessageTrigger;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MessageTrigger.class, name = "MessageTrigger")
})
public interface Trigger {

}
