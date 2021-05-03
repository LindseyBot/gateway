package net.notfab.lindsey.core.automod;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.lindseybot.enums.AutoModFeature;
import org.jetbrains.annotations.NotNull;

public interface AutoModerator {

    AutoModFeature feature();

    boolean moderate(@NotNull Message message, @NotNull Member author);

}
