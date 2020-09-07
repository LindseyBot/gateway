package net.notfab.lindsey.framework.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    private OptionType type;
    private String name;
    private String description;

    @JsonProperty("default")
    private String fallback;

    @JsonIgnore
    public void set(Guild guild, Object value) throws IllegalArgumentException {
        OptionManager.getInstance().set(this, guild, value);
    }

    @JsonIgnore
    public boolean check(Guild guild, Object value) {
        return this.type.check(guild, value);
    }

    @JsonIgnore
    public <T> T get(Guild guild) {
        return OptionManager.getInstance().get(this, guild);
    }

}
