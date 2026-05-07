package org.example;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

public class DemoWindow extends JFrame {
    private final List<MovingObject> objects = new ArrayList<>();
    private final DemoPanel panel;

    public DemoWindow() {
        setTitle("Демонстрационное окно (ДО)");
        setSize(600, 500);
        setLocation(450, 100);
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panel = new DemoPanel();
        add(panel);
    }

    public void requestRepaint() {
        panel.repaint();
    }

    public void addObject(MovingObject obj) {
        synchronized (objects) {
            objects.add(obj);
        }
        obj.start();
    }

    public MovingObject findByNumber(int number) {
        synchronized (objects) {
            for (MovingObject obj : objects) {
                if (obj.getNumber() == number) return obj;
            }
        }
        return null;
    }

    public boolean numberExists(int number) {
        return findByNumber(number) != null;
    }

    public int getObjectCount() {
        synchronized (objects) {
            return objects.size();
        }
    }

    public int getFieldWidth() {
        return panel.getWidth();
    }

    public int getFieldHeight() {
        return panel.getHeight();
    }

    public FontMetrics getFieldFontMetrics() {
        return panel.getFontMetrics(MovingObject.getDrawFont());
    }

    private class DemoPanel extends JPanel {
        DemoPanel() {
            setBackground(Color.WHITE);
            setDoubleBuffered(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            synchronized (objects) {
                for (MovingObject obj : objects) {
                    obj.draw(g2);
                }
            }
            java.awt.Toolkit.getDefaultToolkit().sync();
        }
    }
}
