package net.notfab.lindsey.core.commands.wiki;

import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HScardTest {

    private HScard command;

    @BeforeEach
    void setUp() throws Exception {
        command = mock(HScard.class);
        when(command.execute(null, null, new String[0], null))
                .thenReturn(false);
        when(command.execute(null, null, new String[]{}, null))
                .thenReturn(true);
        when(command.getInfo()).thenCallRealMethod();
        when(command.help(null)).thenCallRealMethod();
    }

    @Test
    void getInfo() {
        CommandDescriptor info = command.getInfo();
        assertEquals("hscard", info.getName(), "Name must be hscard");
        assertEquals(Modules.GAME_WIKI, info.getModule(), "Module must be game wiki");
        assertTrue(info.getPermissions().containsKey("commands." + info.getName()), "Must have permission with command name");
    }

    @Test
    void execute() throws Exception {
        assertTrue(command.execute(null, null, new String[0], null), "No arguments must execute");
    }

    @Test
    void help() throws Exception {
        HelpArticle article = command.help(null);
        HelpPage page = article.get("hscard");
        assertNotNull(page, "Help page must not be null");
        assertEquals("commands." + command.getInfo().getName(), page.getPermission(), "Permission must be command name");
    }
}