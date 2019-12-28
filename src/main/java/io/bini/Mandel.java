package io.bini;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mandel extends JPanel {
    private double initialXRange = 2.7;
    private double initialYRange = 2.4;
    private double initialXCenter = -0.75;
    private double initialYCenter = 0;
    @Getter
    private int initialWidth = 900;
    @Getter
    private int initialHeight = 800;
    private double initialRatio = initialWidth * 1. / initialHeight;
    @Getter
    @Setter
    private double zoom = 1;

    public void changeOffset(int xPixelOffset, int yPixelOffset) {
        Dimension size = getSize();
        double imageWidth = size.getWidth();
        double imageHeight = size.getHeight();
        double xRange = initialXRange * zoom;
        double yRange = initialYRange * zoom;
        initialXCenter += xPixelOffset / imageWidth * xRange;
        initialYCenter += yPixelOffset / imageHeight * yRange;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        long start = System.nanoTime();
        Graphics2D g2d = (Graphics2D) g;
        Dimension size = getSize();
        double imageWidth = size.getWidth();
        double imageHeight = size.getHeight();
        double xRange = initialXRange * zoom;
        double yRange = initialYRange * zoom;
        double x1 = initialXCenter - xRange / 2;
        double x2 = initialXCenter + xRange / 2;
        double y1 = initialYCenter - yRange / 2;
        double y2 = initialYCenter + yRange / 2;
        double newRatio = imageWidth / imageHeight;
        if (newRatio / initialRatio > 1) {
            double newXRange = xRange * newRatio / initialRatio;
            x2 = x2 + (newXRange - xRange) / 2;
            x1 = x1 - (newXRange - xRange) / 2;
        } else if (newRatio / initialRatio < 1) {
            double newYRange = yRange * initialRatio / newRatio;
            y2 = y2 + (newYRange - yRange) / 2;
            y1 = y1 - (newYRange - yRange) / 2;
        }
        double zoomX = imageWidth / (x2 - x1);
        double zoomY = imageHeight / (y2 - y1);

        int iterations = 255;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                double c_r = x / zoomX + x1;
                double c_i = y / zoomY + y1;
                double z_r = 0;
                double z_i = 0;
                int i = 0;
                do {
                    double currentZ_r = z_r;
                    z_r = z_r * z_r - z_i * z_i + c_r;
                    z_i = 2 * z_i * currentZ_r + c_i;
                    i++;
                } while (i < iterations && z_r * z_r + z_i * z_i < 4);
                if (i == iterations) {
                    g2d.setColor(Color.BLACK);
                } else {
                    Color color = new Color(i * 255 / iterations, i * 255 / iterations, i * 255 / iterations);
                    g2d.setColor(color);
                }
                g2d.drawLine(x, y, x, y);
            }
        }
        long end = System.nanoTime();
        System.out.println("Time to generate image: " + (end - start) / 1_000_000 + "ms");
    }

    public static void main(String[] args) {
        Mandel mandel = new Mandel();
        JFrame frame = new JFrame("Mandel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(mandel);
        frame.setSize(mandel.getInitialWidth(), mandel.getInitialHeight());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        mandel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                double width = mandel.getSize().getWidth();
                double height = mandel.getSize().getHeight();
                mandel.changeOffset((int) (e.getX() - width / 2), (int) (e.getY() - height / 2));
                mandel.repaint();
            }
        });
        mandel.addMouseWheelListener(e -> {
            if (e.getUnitsToScroll() > 0) {
                mandel.setZoom(mandel.getZoom() * 1.1);
            } else {
                mandel.setZoom(mandel.getZoom() * 0.9);
            }
            mandel.repaint();
        });
    }
}
