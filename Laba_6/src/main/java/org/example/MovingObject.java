package org.example;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class MovingObject implements Runnable {
    private volatile double x, y;
    private volatile double vx, vy;
    private volatile Color color;
    private final String text;
    private final int number;
    private static final Font FONT = new Font("Dialog", Font.BOLD, 18);

    private final Thread thread;
    private volatile boolean running = true;
    private final DemoWindow demo;
    private long lastStepNanos;

    public MovingObject(DemoWindow demo, String text, int number, Color color,
                        double speed, double angle) {
        this.demo = demo;
        this.text = text;
        this.number = number;
        this.color = color;
        this.x = 5;
        this.y = 25;
        setSpeedAndAngle(speed, angle);
        this.thread = new Thread(this, "obj-" + number);
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        running = false;
        thread.interrupt();
    }

    public synchronized void setSpeedAndAngle(double speed, double angle) {
        this.vx = speed * Math.cos(angle);
        this.vy = speed * Math.sin(angle);
    }

    public synchronized double getSpeed() {
        return Math.sqrt(vx * vx + vy * vy);
    }

    public synchronized void setSpeed(double speed) {
        double cur = Math.sqrt(vx * vx + vy * vy);
        if (cur < 1e-6) {
            vx = speed;
            vy = 0;
        } else {
            vx = vx / cur * speed;
            vy = vy / cur * speed;
        }
    }

    @Override
    public void run() {
        lastStepNanos = System.nanoTime();
        while (running) {
            int w = demo.getFieldWidth();
            int h = demo.getFieldHeight();
            FontMetrics fm = demo.getFieldFontMetrics();
            long now = System.nanoTime();
            double dt = (now - lastStepNanos) / 1_000_000_000.0;
            lastStepNanos = now;
            if (w > 0 && h > 0 && fm != null) {
                step(w, h, fm, dt);
                demo.requestRepaint();
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                if (!running) return;
            }
        }
    }

    private synchronized void step(int w, int h, FontMetrics fm, double dt) {
        x += vx * dt;
        y += vy * dt;

        String label = "(" + number + ") " + text;
        int tw = fm.stringWidth(label);
        int ascent = fm.getAscent();
        int descent = fm.getDescent();

        if (x < 0) {
            x = -x;
            vx = -vx;
        }
        if (x + tw > w) {
            x = 2.0 * (w - tw) - x;
            vx = -vx;
        }
        if (y - ascent < 0) {
            y = 2.0 * ascent - y;
            vy = -vy;
        }
        if (y + descent > h) {
            y = 2.0 * (h - descent) - y;
            vy = -vy;
        }

        if (x < 0) x = 0;
        if (x + tw > w) x = Math.max(0, w - tw);
        if (y - ascent < 0) y = ascent;
        if (y + descent > h) y = Math.max(ascent, h - descent);
    }

    public void draw(Graphics2D g) {
        g.setFont(FONT);
        g.setColor(color);
        g.drawString("(" + number + ") " + text, (float) x, (float) y);
    }

    public int getNumber() { return number; }
    public void setColor(Color c) { this.color = c; }
    public String getText() { return text; }

    public static Font getDrawFont() { return FONT; }
}
