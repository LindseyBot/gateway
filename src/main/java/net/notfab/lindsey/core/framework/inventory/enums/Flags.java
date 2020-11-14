package net.notfab.lindsey.core.framework.inventory.enums;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public enum Flags {

    Australia, Belgium, Brazil, Brunei, Canada, China, Denmark, France, Germany, Honduras, India, Ireland, Japan,
    Korea, Mexico, Netherlands, NewZealand, Norway, Portugal, Russia, SaudiArabia, Sweden, Switzerland, Taiwan,
    UK, Unknown, USA;

    public BufferedImage getImage() {
        try {
            return ImageIO.read(getClass().getResourceAsStream("/assets/templates/flags/" + name() + ".png"));
        } catch (IOException e) {
            return null;
        }
    }

    public static Flags fromString(String value) {
        for (Flags country : Flags.values()) {
            if (country.name().equalsIgnoreCase(value)) {
                return country;
            }
        }
        return Flags.Unknown;
    }

}
