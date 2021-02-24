package net.notfab.lindsey.core.commands.playlist;

import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayListCmdTest {

    private PlayListCmd command;

    @BeforeEach
    void setUp() {
        command = mock(PlayListCmd.class);
        when(command.getInfo())
            .thenCallRealMethod();
        when(command.help(null))
            .thenCallRealMethod();
    }

    @Test
    void getInfo() {
        CommandDescriptor info = command.getInfo();
        assertEquals("playlist", info.getName(), "Name must be playlist");
        assertEquals(Modules.MUSIC, info.getModule(), "Module must be Music");
        assertTrue(info.getPermissions().stream()
            .anyMatch(perm -> perm.getName().equals("commands." + info.getName())), "Must have permission with command name");
    }

    @Test
    void help() {
        HelpArticle article = command.help(null);
        HelpPage page = article.get("playlist");
        assertNotNull(page, "Help page must not be null");
        assertEquals("commands." + command.getInfo().getName(), page.getPermission(), "Permission must be command name");
    }

}
