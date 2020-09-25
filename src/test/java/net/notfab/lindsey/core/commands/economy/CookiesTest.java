package net.notfab.lindsey.core.commands.economy;

import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CookiesTest {

    private Cookies command;

    @BeforeEach
    void setUp() {
        command = mock(Cookies.class);
        when(command.getInfo())
            .thenCallRealMethod();
        when(command.help(null))
            .thenCallRealMethod();
    }

    @Test
    void getInfo() {
        CommandDescriptor info = command.getInfo();
        assertEquals("cookies", info.getName(), "Name must be cookies");
        assertEquals(Modules.ECONOMY, info.getModule(), "Module must be economy");
        assertTrue(info.getPermissions().stream()
            .anyMatch(perm -> perm.getName().equals("commands." + info.getName())), "Must have permission with command name");
    }

    @Test
    void help() {
        HelpArticle article = command.help(null);
        HelpPage page = article.get("cookies");
        assertNotNull(page, "Help page must not be null");
        assertEquals("commands." + command.getInfo().getName(), page.getPermission(), "Permission must be command name");
    }

}
