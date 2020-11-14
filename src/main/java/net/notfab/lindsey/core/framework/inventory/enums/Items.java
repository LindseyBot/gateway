package net.notfab.lindsey.core.framework.inventory.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.notfab.lindsey.core.framework.inventory.ItemMeta;
import org.json.JSONException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * All Lindsey items.
 */
public enum Items {

    BADGE_HALLOWEEN_2017("badges/halloween/2017"),
    BADGE_HALLOWEEN_2018("badges/halloween/2018"),
    BADGE_HALLOWEEN_2019("badges/halloween/2019"),
    BADGE_TRANSLATOR("badges/translator"),
    BADGE_VIP("badges/vip"),
    BADGE_FBI("badges/fbi"),
    BADGE_BETA("badges/beta"),
    ;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String filePath;
    private ItemMeta meta;

    Items(String filePath) {
        this.filePath = filePath;
    }

    private String getBaseAsset() {
        return "/assets/" + this.filePath;
    }

    public BufferedImage getImage() {
        try {
            return ImageIO.read(getClass().getResourceAsStream(getBaseAsset() + ".png"));
        } catch (IOException e) {
            return null;
        }
    }

    public ItemMeta getMetadata() {
        if (this.meta != null) {
            return this.meta;
        }
        try (InputStream stream = getClass().getResourceAsStream(getBaseAsset() + ".json")) {
            this.meta = objectMapper.readValue(stream, ItemMeta.class);
            return this.meta;
        } catch (IOException | JSONException ex) {
            return null;
        }
    }

    public static Items find(String name) {
        for (Items item : Items.values()) {
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
            ItemMeta meta = item.getMetadata();
            if (meta == null) {
                continue;
            }
            if (meta.getName().toLowerCase().contains(name.toLowerCase())) {
                return item;
            }
            if (meta.getTags().contains(name.toLowerCase())) {
                return item;
            }
            if (meta.getDescription().toLowerCase().contains(name.toLowerCase())) {
                return item;
            }
            if (meta.getType().name().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

}
