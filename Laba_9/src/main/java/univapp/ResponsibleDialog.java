package univapp;

import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResponsibleDialog extends JDialog {
    private final JTextField idField = new JTextField(10);
    private final JTextField fioField = new JTextField(20);
    private final JTextField positionField = new JTextField(20);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField ageField = new JTextField(5);

    private final String mode;
    private boolean accepted = false;

    public ResponsibleDialog(JFrame owner, String mode, Integer existingId) {
        super(owner, modeTitle(mode), true);
        this.mode = mode;

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("ID (уникальный ключ):"));
        form.add(idField);
        form.add(new JLabel("ФИО:"));
        form.add(fioField);
        form.add(new JLabel("Должность:"));
        form.add(positionField);
        form.add(new JLabel("Телефон:"));
        form.add(phoneField);
        form.add(new JLabel("Возраст:"));
        form.add(ageField);

        if ("create".equals(mode)) {
            idField.setText(String.valueOf(nextId()));
            fioField.setText(Defaults.get("responsible.fio", ""));
            positionField.setText(Defaults.get("responsible.position", ""));
            phoneField.setText(Defaults.get("responsible.phone", ""));
            ageField.setText(Defaults.get("responsible.age", "0"));
        } else if (existingId != null) {
            loadFromDb(existingId);
            idField.setEditable(false);
        }

        if ("view".equals(mode)) {
            setEditable(false);
        }

        JButton resetBtn = new JButton("Сброс к умолчанию");
        resetBtn.addActionListener(e -> resetToDefaults());

        JButton okBtn = new JButton("Принять");
        okBtn.addActionListener(e -> onAccept());
        JButton cancelBtn = new JButton("Отменить");
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel();
        if (!"view".equals(mode)) {
            buttons.add(resetBtn);
            buttons.add(okBtn);
        }
        buttons.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private static String modeTitle(String mode) {
        return switch (mode) {
            case "create" -> "Новый ответственный";
            case "edit" -> "Редактирование ответственного";
            case "view" -> "Просмотр ответственного";
            default -> "Ответственный";
        };
    }

    private void setEditable(boolean editable) {
        idField.setEditable(editable);
        fioField.setEditable(editable);
        positionField.setEditable(editable);
        phoneField.setEditable(editable);
        ageField.setEditable(editable);
    }

    private void resetToDefaults() {
        fioField.setText(Defaults.get("responsible.fio", ""));
        positionField.setText(Defaults.get("responsible.position", ""));
        phoneField.setText(Defaults.get("responsible.phone", ""));
        ageField.setText(Defaults.get("responsible.age", "0"));
    }

    private int nextId() {
        try (Statement st = Db.get().createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(ID) FROM RESPONSIBLES")) {
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return 1;
    }

    private void loadFromDb(int id) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "SELECT * FROM RESPONSIBLES WHERE ID=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idField.setText(String.valueOf(rs.getInt("ID")));
                    fioField.setText(rs.getString("FIO"));
                    positionField.setText(rs.getString("POSITION"));
                    phoneField.setText(rs.getString("PHONE"));
                    ageField.setText(String.valueOf(rs.getInt("AGE")));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + e.getMessage());
        }
    }

    private void onAccept() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String fio = fioField.getText().trim();
            String position = positionField.getText().trim();
            String phone = phoneField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            if (fio.isEmpty() || position.isEmpty() || phone.isEmpty() || age <= 0) {
                JOptionPane.showMessageDialog(this, "Заполните все поля корректно");
                return;
            }

            if ("create".equals(mode)) {
                try (PreparedStatement ps = Db.get().prepareStatement(
                        "INSERT INTO RESPONSIBLES (ID, FIO, POSITION, PHONE, AGE) VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, fio);
                    ps.setString(3, position);
                    ps.setString(4, phone);
                    ps.setInt(5, age);
                    ps.executeUpdate();
                }
            } else if ("edit".equals(mode)) {
                try (PreparedStatement ps = Db.get().prepareStatement(
                        "UPDATE RESPONSIBLES SET FIO=?, POSITION=?, PHONE=?, AGE=? WHERE ID=?")) {
                    ps.setString(1, fio);
                    ps.setString(2, position);
                    ps.setString(3, phone);
                    ps.setInt(4, age);
                    ps.setInt(5, id);
                    ps.executeUpdate();
                }
            }
            accepted = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректные числовые значения");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка БД: " + e.getMessage());
        }
    }

    public boolean isAccepted() {
        return accepted;
    }
}
