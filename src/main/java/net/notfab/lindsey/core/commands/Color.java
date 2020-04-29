package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Color implements Command {

    private static final Logger logger = LoggerFactory.getLogger(Color.class);

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("color")
                .module(Modules.FUN)
                .permission("commands.color", "Permission to use the base command")
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {
        if (args.length == 0) {
            return false;
        } else {
            int color;
            try {
                color = Integer.parseInt((args[0].replace("#", "")), 16);
            } catch (IllegalArgumentException ex) {
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
                logger.error("Error writing color", ex);
                return false;
            }
            channel.sendFile(new ByteArrayInputStream(os.toByteArray()), color + ".png").queue();
        }
        return false;
    }

}
