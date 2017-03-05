import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel(final String imagePath) {
        super();

        try {
            final URL url = getClass().getResource(imagePath);
            if (url != null) {
                image = ImageIO.read(url);
            } else {
                throw new RuntimeException("Image URL is null for path=" + imagePath);
            }
        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }
    }

    protected void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(image, 0, 0, this.getWidth(), this.getHeight(),this);
    }
}
