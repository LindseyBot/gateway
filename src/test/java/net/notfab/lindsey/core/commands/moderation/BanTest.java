package net.notfab.lindsey.core.commands.moderation;

import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BanTest {

    private Ban command;

    @BeforeEach
    void setUp() {
        command = mock(Ban.class);
        when(command.getInfo())
                .thenCallRealMethod();
        when(command.help(null))
                .thenCallRealMethod();
    }

    @Test
    void getInfo() {
        CommandDescriptor info = command.getInfo();
        assertEquals("ban", info.getName(), "Name must be ban");
        assertEquals(Modules.MODERATION, info.getModule(), "Module must be Moderation");
        assertTrue(info.getPermissions().containsKey("commands." + info.getName()), "Must have permission with command name");
    }

    @Test
    void help() {
        HelpArticle article = command.help(null);
        HelpPage page = article.get("ban");
        assertNotNull(page, "Help page must not be null");
        assertEquals("commands." + command.getInfo().getName(), page.getPermission(), "Permission must be command name");
    }

}
