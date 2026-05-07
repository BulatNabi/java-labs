package org.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DemoWindow demo = new DemoWindow();
            ControlWindow control = new ControlWindow(demo);
            demo.setVisible(true);
            control.setVisible(true);
        });
    }
}
