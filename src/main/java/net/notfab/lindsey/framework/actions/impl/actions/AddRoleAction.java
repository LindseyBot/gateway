package net.notfab.lindsey.framework.actions.impl.actions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.*;
import net.notfab.lindsey.framework.actions.Action;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddRoleAction implements Action {

    private String roleId;

    @Override
    public void act(Guild guild, Member member, TextChannel channel, Message message) {
        if (member == null) {
            return;
        }
        if (roleId == null || roleId.isEmpty()) {
            return;
        }
        Role role = guild.getRoleById(roleId);
        if (role == null) {
            return;
        }
        if (!guild.getSelfMember().canInteract(role)) {
            return;
        }
        if (!guild.getSelfMember().canInteract(member)) {
            return;
        }
        if (member.getRoles().contains(role)) {
            return;
        }
        guild.addRoleToMember(member, role).queue();
    }

}
