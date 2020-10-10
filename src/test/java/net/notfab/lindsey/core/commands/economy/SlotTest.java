package net.notfab.lindsey.core.commands.economy;

import net.notfab.lindsey.core.commands.economy.Slot;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SlotTest {

    private Slot command;

    @BeforeEach
    void setUp() {
        command = mock(Slot.class);
        when(command.getInfo())
                .thenCallRealMethod();
        when(command.help(null))
                .thenCallRealMethod();
    }

    @Test
    void getInfo() {
        CommandDescriptor info = command.getInfo();
        assertEquals("slot", info.getName(), "Name must be slot");
        assertEquals(Modules.ECONOMY, info.getModule(), "Module must be fun");
        assertTrue(info.getPermissions().stream()
                .anyMatch(perm -> perm.getName().equals("commands." + info.getName())), "Must have permission with command name");
    }

    @Test
    void help() {
        HelpArticle article = command.help(null);
        HelpPage page = article.get("slot");
        assertNotNull(page, "Help page must not be null");
        assertEquals("commands." + command.getInfo().getName(), page.getPermission(), "Permission must be command name");
    }

}
