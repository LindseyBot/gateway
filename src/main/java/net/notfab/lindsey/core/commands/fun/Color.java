package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class Color implements Command {

    private static final Logger logger = LoggerFactory.getLogger(Color.class);

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("color")
            .module(Modules.FUN)
            .permission("commands.color", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        } else {
            int color;
            try {
                color = Integer.parseInt((args[0].replace("#", "")), 16);
            } catch (IllegalArgumentException ex) {
                msg.send(channel, sender(member) + i18n.get(member, ""));
                return false;
            }
            BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setPaint(new java.awt.Color(color));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", os);
            } catch (IOException ex) {
                logger.error("Error while writing color", ex);
                return false;
            }
            channel.sendFile(new ByteArrayInputStream(os.toByteArray()), color + ".png").queue();
        }
        return false;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("color")
            .text("commands.fun.color.description")
            .usage("L!color <#RGB>")
            .permission("commands.color")
            .addExample("L!color ff00aa");
        return HelpArticle.of(page);
    }

}
