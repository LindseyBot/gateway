package net.notfab.lindsey.core.framework;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GFXUtils {

    public static Color RED = new Color(210, 47, 47);
    public static Color BLUE = new Color(27, 137, 255);
    public static Color YELLOW = new Color(255, 255, 0);
    public static Color GREEN = new Color(0, 204, 0);

    /**
     * Crops an image by using the given rectangle (new Rectangle(startX, startY, width, height).
     *
     * @param src  - Source BufferedImage;
     * @param rect - Rectangle to use;
     * @return cropped BufferedImage.
     */
    public static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        return src.getSubimage(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Resizes an image via AffineTransform (to the given scale).
     *
     * @param original - The image to resize;
     * @param scaleX   - The X pixel scale to use (Rec: 0.4);
     * @param scaleY   - The Y pixel scale to use (Rec: 0.4);
     * @return Resized BufferedImage.
     */
    public static BufferedImage resize(BufferedImage original, double scaleX, double scaleY) {
        BufferedImage resizedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
        g.drawRenderedImage(original, at);
        g.dispose();
        return resizedImage;
    }

    /**
     * Resizes an image.
     *
     * @param originalImage - The image to resize;
     * @param IMG_WIDTH     - The new width;
     * @param IMG_HEIGHT    - The new height;
     * @return resized BufferedImage.
     */
    public static BufferedImage resize(BufferedImage originalImage, int IMG_WIDTH, int IMG_HEIGHT) {
        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
        return resizedImage;
    }

    /**
     * Returns the average color of an image (stolen from Kaaz / EmilyBot)
     *
     * @param url the url to get the image
     * @return average color OR fallback color in case of invalid url
     */
    public static Color getAverageColor(String url) {
        try {
            Request request = new Request.Builder().url(url).header("User-Agent", "LewdBot").build();
            Response response = new OkHttpClient().newCall(request).execute();
            ResponseBody body = response.body();
            if (body == null) {
                response.close();
                return GFXUtils.RED;
            }
            InputStream inputStream = Utils.cloneStream(body.byteStream());
            response.close();
            if (inputStream == null) return GFXUtils.RED;
            return getAverageColor(inputStream);
        } catch (IOException e) {
            return GFXUtils.RED;
        }
    }

    /**
     * Returns the average color of an image (stolen from Kaaz / EmilyBot)
     *
     * @param inputStream The inputStream for the image
     * @return average color OR fallback color in case of invalid stream
     */
    public static Color getAverageColor(InputStream inputStream) {
        try {
            return getAverageColor(ImageIO.read(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return GFXUtils.RED;
        }
    }

    /**
     * Returns the average color of an image (stolen from Kaaz / EmilyBot)
     *
     * @param image BufferedImage
     * @return average color OR fallback color in case of invalid stream
     */
    public static Color getAverageColor(BufferedImage image) {
        try {
            if (image == null) return GFXUtils.RED;
            int x0 = 0;
            int y0 = 0;
            int x1 = x0 + image.getWidth();
            int y1 = y0 + image.getHeight();
            long sumr = 0, sumg = 0, sumb = 0;
            for (int x = x0; x < x1; x++) {
                for (int y = y0; y < y1; y++) {
                    Color pixel = new Color(image.getRGB(x, y));
                    sumr += pixel.getRed();
                    sumg += pixel.getGreen();
                    sumb += pixel.getBlue();
                }
            }
            int num = image.getWidth() * image.getHeight();
            return new Color((int) sumr / num, (int) sumg / num, (int) sumb / num);
        } catch (IllegalArgumentException | NullPointerException e) {
            return GFXUtils.RED;
        }
    }

    /**
     * @param hex - Hex string. eg: #FFFFFF
     * @return Color object.
     */
    public static Color getColor(String hex) {
        return new Color(
            Integer.valueOf(hex.substring(1, 3), 16),
            Integer.valueOf(hex.substring(3, 5), 16),
            Integer.valueOf(hex.substring(5, 7), 16));
    }

}
