package net.notfab.lindsey.core.commands.nsfw;

import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KonachanTest {

    private Konachan command;

    @BeforeEach
    void setUp() {
        command = mock(Konachan.class);
        when(command.getInfo())
                .thenCallRealMethod();
        when(command.help(null))
                .thenCallRealMethod();
    }

    @Test
    void getInfo() {
        CommandDescriptor info = command.getInfo();
        assertEquals("konachan", info.getName(), "Name must be konachan");
        assertEquals(Modules.NSFW, info.getModule(), "Module must be Nsfw");
        assertTrue(info.getPermissions().stream()
                .anyMatch(perm -> perm.getName().equals("commands." + info.getName())), "Must have permission with command name");
    }

    @Test
    void help() {
        HelpArticle article = command.help(null);
        HelpPage page = article.get("konachan");
        assertNotNull(page, "Help page must not be null");
        assertEquals("commands." + command.getInfo().getName(), page.getPermission(), "Permission must be command name");
    }

}
