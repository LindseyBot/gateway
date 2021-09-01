package net.notfab.lindsey.core.commands.moderation;

import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HackbanTest {

    private Hackban command;

    @BeforeEach
    void setUp() {
        command = mock(Hackban.class);
        when(command.getInfo())
                .thenCallRealMethod();
        when(command.help(null))
                .thenCallRealMethod();
    }

    @Test
    void getInfo() {
        CommandDescriptor info = command.getInfo();
        assertEquals("hackban", info.getName(), "Name must be hackban");
        assertEquals(Modules.MODERATION, info.getModule(), "Module must be Moderation");
        assertTrue(info.getPermissions().stream()
                .anyMatch(perm -> perm.getName().equals("commands.ban")), "Must use shared ban permission");
    }

    @Test
    void help() {
        HelpArticle article = command.help(null);
        HelpPage page = article.get("hackban");
        assertNotNull(page, "Help page must not be null");
        assertEquals("commands.ban", page.getPermission(), "Permission must be shared");
    }

}
