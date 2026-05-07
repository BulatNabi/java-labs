package org.example;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Random;

public class ControlWindow extends JFrame {
    private final DemoWindow demo;
    private final Random rnd = new Random();

    private static final int MAX_OBJECTS = 5;

    private final JTextField textField = new JTextField("Объект", 12);
    private final JTextField numberField = new JTextField("1", 12);
    private final JTextField speedField = new JTextField("150", 12);

    private final JComboBox<String> colorCombo = new JComboBox<>(new String[]{
            "Красный", "Синий", "Зелёный", "Жёлтый", "Чёрный", "Оранжевый", "Розовый"
    });
    private final Color[] colorValues = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK,
            Color.ORANGE, Color.PINK
    };

    private final JTextField selectNumberField = new JTextField(12);
    private final JComboBox<String> adjustColorCombo = new JComboBox<>(new String[]{
            "Красный", "Синий", "Зелёный", "Жёлтый", "Чёрный", "Оранжевый", "Розовый"
    });
    private final JComboBox<String> adjustSpeedCombo = new JComboBox<>(new String[]{
            "Очень медленно", "Медленно", "Средне", "Быстро", "Очень быстро"
    });
    private final double[] adjustSpeeds = {40, 100, 180, 300, 450};

    public ControlWindow(DemoWindow demo) {
        this.demo = demo;
        setTitle("Управляющее окно (УО)");
        setLocation(20, 100);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        root.add(header("Запуск нового ФиО (макс. " + MAX_OBJECTS + ")"), c);

        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        root.add(new JLabel("Текст ФиО:"), c);
        c.gridx = 1;
        root.add(textField, c);

        c.gridy++;
        c.gridx = 0;
        root.add(new JLabel("Номер ФиО:"), c);
        c.gridx = 1;
        root.add(numberField, c);

        c.gridy++;
        c.gridx = 0;
        root.add(new JLabel("Цвет:"), c);
        c.gridx = 1;
        root.add(colorCombo, c);

        c.gridy++;
        c.gridx = 0;
        root.add(new JLabel("Нач. скорость (пикс/с):"), c);
        c.gridx = 1;
        root.add(speedField, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        JButton launchBtn = new JButton("Пуск");
        launchBtn.addActionListener(e -> launchObject());
        root.add(launchBtn, c);

        c.gridy++;
        root.add(header("Изменение существующего ФиО"), c);

        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        root.add(new JLabel("Номер ФиО:"), c);
        c.gridx = 1;
        root.add(selectNumberField, c);

        c.gridy++;
        c.gridx = 0;
        root.add(new JLabel("Новый цвет:"), c);
        c.gridx = 1;
        root.add(adjustColorCombo, c);

        c.gridy++;
        c.gridx = 0;
        root.add(new JLabel("Новая скорость:"), c);
        c.gridx = 1;
        root.add(adjustSpeedCombo, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        JButton applyBtn = new JButton("Применить");
        applyBtn.addActionListener(e -> applyChanges());
        root.add(applyBtn, c);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(360, getHeight()));
    }

    private JLabel header(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        return l;
    }

    private void launchObject() {
        if (demo.getObjectCount() >= MAX_OBJECTS) {
            JOptionPane.showMessageDialog(this,
                    "Достигнут максимум ФиО (" + MAX_OBJECTS + ")");
            return;
        }

        String text = textField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите текст ФиО");
            return;
        }

        int number;
        try {
            number = Integer.parseInt(numberField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректный номер");
            return;
        }
        if (demo.numberExists(number)) {
            JOptionPane.showMessageDialog(this,
                    "ФиО с номером " + number + " уже существует");
            return;
        }

        double speed;
        try {
            speed = Double.parseDouble(speedField.getText().trim());
            if (speed <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Некорректная скорость (введите положительное число)");
            return;
        }

        Color color = colorValues[colorCombo.getSelectedIndex()];

        double angle;
        do {
            angle = 0.05 + rnd.nextDouble() * (Math.PI / 2 - 0.1);
        } while (Math.abs(angle - Math.PI / 4) < 0.05);

        MovingObject obj = new MovingObject(demo, text, number, color, speed, angle);
        demo.addObject(obj);

        int next = number + 1;
        while (demo.numberExists(next)) next++;
        numberField.setText(String.valueOf(next));
    }

    private void applyChanges() {
        int num;
        try {
            num = Integer.parseInt(selectNumberField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректный номер ФиО");
            return;
        }
        MovingObject obj = demo.findByNumber(num);
        if (obj == null) {
            JOptionPane.showMessageDialog(this,
                    "ФиО с номером " + num + " не найден");
            return;
        }

        obj.setColor(colorValues[adjustColorCombo.getSelectedIndex()]);
        obj.setSpeed(adjustSpeeds[adjustSpeedCombo.getSelectedIndex()]);
    }
}
