package univapp;

import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDialog extends JDialog {
    private final JTextField idField = new JTextField(10);
    private final JTextField buildingField = new JTextField(20);
    private final JTextField roomNumberField = new JTextField(10);
    private final JTextField nameField = new JTextField(20);
    private final JTextField areaField = new JTextField(10);
    private final JComboBox<RespItem> responsibleCombo = new JComboBox<>();

    private final String mode;
    private boolean accepted = false;

    public ClassroomDialog(JFrame owner, String mode, Integer existingId) {
        super(owner, modeTitle(mode), true);
        this.mode = mode;

        loadResponsibles();

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("ID (уникальный ключ):"));
        form.add(idField);
        form.add(new JLabel("Здание:"));
        form.add(buildingField);
        form.add(new JLabel("Номер аудит.:"));
        form.add(roomNumberField);
        form.add(new JLabel("Наименование:"));
        form.add(nameField);
        form.add(new JLabel("Площадь:"));
        form.add(areaField);
        form.add(new JLabel("Ответственный:"));
        form.add(responsibleCombo);

        if ("create".equals(mode)) {
            idField.setText(String.valueOf(nextId()));
            buildingField.setText(Defaults.get("classroom.building", ""));
            roomNumberField.setText(Defaults.get("classroom.room_number", ""));
            nameField.setText(Defaults.get("classroom.name", ""));
            areaField.setText(Defaults.get("classroom.area", "0"));
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
            case "create" -> "Новая аудитория";
            case "edit" -> "Редактирование аудитории";
            case "view" -> "Просмотр аудитории";
            default -> "Аудитория";
        };
    }

    private void setEditable(boolean editable) {
        idField.setEditable(editable);
        buildingField.setEditable(editable);
        roomNumberField.setEditable(editable);
        nameField.setEditable(editable);
        areaField.setEditable(editable);
        responsibleCombo.setEnabled(editable);
    }

    private void resetToDefaults() {
        buildingField.setText(Defaults.get("classroom.building", ""));
        roomNumberField.setText(Defaults.get("classroom.room_number", ""));
        nameField.setText(Defaults.get("classroom.name", ""));
        areaField.setText(Defaults.get("classroom.area", "0"));
    }

    private int nextId() {
        try (Statement st = Db.get().createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(ID) FROM CLASSROOMS")) {
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return 1;
    }

    private void loadResponsibles() {
        responsibleCombo.removeAllItems();
        try (Statement st = Db.get().createStatement();
             ResultSet rs = st.executeQuery("SELECT ID, FIO FROM RESPONSIBLES ORDER BY FIO")) {
            while (rs.next()) {
                responsibleCombo.addItem(new RespItem(rs.getInt("ID"), rs.getString("FIO")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки ответственных: " + e.getMessage());
        }
    }

    private void loadFromDb(int id) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "SELECT * FROM CLASSROOMS WHERE ID=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idField.setText(String.valueOf(rs.getInt("ID")));
                    buildingField.setText(rs.getString("BUILDING"));
                    roomNumberField.setText(rs.getString("ROOM_NUMBER"));
                    nameField.setText(rs.getString("NAME"));
                    areaField.setText(String.valueOf(rs.getDouble("AREA")));
                    int respId = rs.getInt("RESPONSIBLE_ID");
                    for (int i = 0; i < responsibleCombo.getItemCount(); i++) {
                        if (responsibleCombo.getItemAt(i).id == respId) {
                            responsibleCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + e.getMessage());
        }
    }

    private void onAccept() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String building = buildingField.getText().trim();
            String roomNum = roomNumberField.getText().trim();
            String name = nameField.getText().trim();
            double area = Double.parseDouble(areaField.getText().trim());
            if (building.isEmpty() || roomNum.isEmpty() || name.isEmpty() || area <= 0) {
                JOptionPane.showMessageDialog(this, "Заполните все поля корректно");
                return;
            }
            RespItem resp = (RespItem) responsibleCombo.getSelectedItem();
            if (resp == null) {
                JOptionPane.showMessageDialog(this, "Выберите ответственного");
                return;
            }

            if ("create".equals(mode)) {
                try (PreparedStatement ps = Db.get().prepareStatement(
                        "INSERT INTO CLASSROOMS (ID, BUILDING, ROOM_NUMBER, NAME, AREA, RESPONSIBLE_ID) " +
                        "VALUES (?,?,?,?,?,?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, building);
                    ps.setString(3, roomNum);
                    ps.setString(4, name);
                    ps.setDouble(5, area);
                    ps.setInt(6, resp.id);
                    ps.executeUpdate();
                }
            } else if ("edit".equals(mode)) {
                try (PreparedStatement ps = Db.get().prepareStatement(
                        "UPDATE CLASSROOMS SET BUILDING=?, ROOM_NUMBER=?, NAME=?, AREA=?, RESPONSIBLE_ID=? WHERE ID=?")) {
                    ps.setString(1, building);
                    ps.setString(2, roomNum);
                    ps.setString(3, name);
                    ps.setDouble(4, area);
                    ps.setInt(5, resp.id);
                    ps.setInt(6, id);
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

    private static class RespItem {
        final int id;
        final String fio;

        RespItem(int id, String fio) {
            this.id = id;
            this.fio = fio;
        }

        @Override
        public String toString() {
            return "#" + id + " " + fio;
        }
    }
}
