package ro.uaic.asli.lab8.service;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MazeImageExporter {

    public void exportComponentToPng(JComponent component, File file) throws IOException {
        if (component == null) {
            throw new IllegalArgumentException("Component is null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File is null");
        }

        int w = Math.max(1, component.getWidth());
        int h = Math.max(1, component.getHeight());

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            component.printAll(g2);
        } finally {
            g2.dispose();
        }
        ImageIO.write(image, "png", file);
    }
}

