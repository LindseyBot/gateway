package net.notfab.lindsey.core.commands.wiki;

import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KickstarterTest {

    private Kickstarter command;

    @BeforeEach
    void setUp() {
        command = mock(Kickstarter.class);
        when(command.getInfo())
            .thenCallRealMethod();
        when(command.help(null))
            .thenCallRealMethod();
    }

    @Test
    void getInfo() {
        CommandDescriptor info = command.getInfo();
        assertEquals("kickstarter", info.getName(), "Name must be kickstarter");
        assertEquals(Modules.GAME_WIKI, info.getModule(), "Module must be game wiki");
        assertTrue(info.getPermissions().stream()
            .anyMatch(perm -> perm.getName().equals("commands." + info.getName())), "Must have permission with command name");
    }

    @Test
    void help() {
        HelpArticle article = command.help(null);
        HelpPage page = article.get("kickstarter");
        assertNotNull(page, "Help page must not be null");
        assertEquals("commands." + command.getInfo().getName(), page.getPermission(), "Permission must be command name");
    }

}
