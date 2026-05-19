гpackage univapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Locale;

public class Main extends JFrame {
    private final DefaultTableModel classroomsModel = new DefaultTableModel(
            new Object[]{"ID", "Здание", "Номер", "Наименование", "Площадь", "Ответственный"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel responsiblesModel = new DefaultTableModel(
            new Object[]{"ID", "ФИО", "Должность", "Телефон", "Возраст"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable classroomsTable = new JTable(classroomsModel);
    private final JTable responsiblesTable = new JTable(responsiblesModel);

    private final JTextField keyField = new JTextField(8);
    private final JTextArea outputArea = new JTextArea(8, 40);

    private final JTabbedPane tabs = new JTabbedPane();

    public Main() {
        super("Лаба 9 — Учебные аудитории");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tabs.addTab("Аудитории", buildClassroomsPanel());
        tabs.addTab("Ответственные", buildResponsiblesPanel());
        add(tabs, BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        try {
            Db.get();
            reloadClassrooms();
            reloadResponsibles();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Не удалось подключиться к БД: " + e.getMessage());
        }

        pack();
        setSize(900, 700);
        setLocationRelativeTo(null);
    }

    private JPanel buildClassroomsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewBtn = new JButton("Просмотр");
        JButton createBtn = new JButton("Создать");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteBtn = new JButton("Удалить");
        viewBtn.addActionListener(e -> openClassroomDialog("view"));
        createBtn.addActionListener(e -> openClassroomDialog("create"));
        editBtn.addActionListener(e -> openClassroomDialog("edit"));
        deleteBtn.addActionListener(e -> deleteClassroom());
        buttons.add(viewBtn);
        buttons.add(createBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);

        p.add(buttons, BorderLayout.NORTH);
        p.add(new JScrollPane(classroomsTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildResponsiblesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewBtn = new JButton("Просмотр");
        JButton createBtn = new JButton("Создать");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteBtn = new JButton("Удалить");
        viewBtn.addActionListener(e -> openResponsibleDialog("view"));
        createBtn.addActionListener(e -> openResponsibleDialog("create"));
        editBtn.addActionListener(e -> openResponsibleDialog("edit"));
        deleteBtn.addActionListener(e -> deleteResponsible());
        buttons.add(viewBtn);
        buttons.add(createBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);

        p.add(buttons, BorderLayout.NORTH);
        p.add(new JScrollPane(responsiblesTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Запросы и сервис"));

        JPanel keyRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keyRow.add(new JLabel("Уникальный ключ записи (для Изменить/Удалить):"));
        keyRow.add(keyField);

        JPanel queries = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton fioListBtn = new JButton("Список ФИО");
        JButton phoneBookBtn = new JButton("Телефонный справочник");
        JButton avgAreaBtn = new JButton("Средняя площадь по ответственным");
        JButton resetDbBtn = new JButton("Сброс БД к умолчанию");
        fioListBtn.addActionListener(e -> queryFioList());
        phoneBookBtn.addActionListener(e -> queryPhoneBook());
        avgAreaBtn.addActionListener(e -> queryAverageArea());
        resetDbBtn.addActionListener(e -> resetDatabase());
        queries.add(fioListBtn);
        queries.add(phoneBookBtn);
        queries.add(avgAreaBtn);
        queries.add(resetDbBtn);

        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.add(keyRow, BorderLayout.NORTH);
        top.add(queries, BorderLayout.SOUTH);

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        return p;
    }

    private Integer selectedKey(JTable table, DefaultTableModel model) {
        String text = keyField.getText().trim();
        if (!text.isEmpty()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Ключ должен быть числом");
                return null;
            }
        }
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (Integer) model.getValueAt(row, 0);
    }

    private void openClassroomDialog(String mode) {
        Integer id = null;
        if (!"create".equals(mode)) {
            id = selectedKey(classroomsTable, classroomsModel);
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Выберите запись или введите ключ");
                return;
            }
        }
        ClassroomDialog d = new ClassroomDialog(this, mode, id);
        d.setVisible(true);
        if (d.isAccepted()) reloadClassrooms();
    }

    private void openResponsibleDialog(String mode) {
        Integer id = null;
        if (!"create".equals(mode)) {
            id = selectedKey(responsiblesTable, responsiblesModel);
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Выберите запись или введите ключ");
                return;
            }
        }
        ResponsibleDialog d = new ResponsibleDialog(this, mode, id);
        d.setVisible(true);
        if (d.isAccepted()) reloadResponsibles();
    }

    private void deleteClassroom() {
        Integer id = selectedKey(classroomsTable, classroomsModel);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Выберите запись или введите ключ");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить аудиторию #" + id + "?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection c = null;
        try {
            c = Db.get();
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM CLASSROOMS WHERE ID=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            c.commit();
            reloadClassrooms();
        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        } finally {
            try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private void deleteResponsible() {
        Integer id = selectedKey(responsiblesTable, responsiblesModel);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Выберите запись или введите ключ");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить ответственного #" + id + " (со всеми его аудиториями)?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection c = null;
        try {
            c = Db.get();
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement(
                    "DELETE FROM CLASSROOMS WHERE RESPONSIBLE_ID=?")) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = c.prepareStatement(
                    "DELETE FROM RESPONSIBLES WHERE ID=?")) {
                ps2.setInt(1, id);
                ps2.executeUpdate();
            }
            c.commit();
            reloadResponsibles();
            reloadClassrooms();
        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        } finally {
            try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private void reloadClassrooms() {
        classroomsModel.setRowCount(0);
        String sql = "SELECT c.ID, c.BUILDING, c.ROOM_NUMBER, c.NAME, c.AREA, r.FIO " +
                "FROM CLASSROOMS c LEFT JOIN RESPONSIBLES r ON c.RESPONSIBLE_ID=r.ID " +
                "ORDER BY c.ID";
        try (Statement st = Db.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                classroomsModel.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5), rs.getString(6)
                });
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void reloadResponsibles() {
        responsiblesModel.setRowCount(0);
        try (Statement st = Db.get().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM RESPONSIBLES ORDER BY ID")) {
            while (rs.next()) {
                responsiblesModel.addRow(new Object[]{
                        rs.getInt("ID"), rs.getString("FIO"), rs.getString("POSITION"),
                        rs.getString("PHONE"), rs.getInt("AGE")
                });
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void queryFioList() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== П.7=1, П.8=0: ФИО второй таблицы в лекс. порядке ===\n");
        try (Statement st = Db.get().createStatement();
             ResultSet rs = st.executeQuery("SELECT FIO FROM RESPONSIBLES ORDER BY FIO")) {
            int n = 1;
            while (rs.next()) {
                sb.append(n++).append(". ").append(rs.getString("FIO")).append('\n');
            }
        } catch (SQLException e) {
            sb.append("Ошибка: ").append(e.getMessage());
        }
        outputArea.setText(sb.toString());
    }

    private void queryPhoneBook() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Вариант д: телефонный справочник в лекс. порядке ===\n");
        sb.append(String.format("%-40s %s%n", "ФИО", "Телефон"));
        sb.append("-".repeat(60)).append('\n');
        try (Statement st = Db.get().createStatement();
             ResultSet rs = st.executeQuery("SELECT FIO, PHONE FROM RESPONSIBLES ORDER BY FIO")) {
            while (rs.next()) {
                sb.append(String.format("%-40s %s%n",
                        rs.getString("FIO"), rs.getString("PHONE")));
            }
        } catch (SQLException e) {
            sb.append("Ошибка: ").append(e.getMessage());
        }
        outputArea.setText(sb.toString());
    }

    private void queryAverageArea() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Вариант д: средняя площадь, закреплённая за ответственными ===\n");
        String sql =
                "SELECT r.FIO, COALESCE(SUM(c.AREA), 0) AS TOTAL " +
                "FROM RESPONSIBLES r LEFT JOIN CLASSROOMS c ON c.RESPONSIBLE_ID = r.ID " +
                "GROUP BY r.ID, r.FIO ORDER BY r.FIO";
        try (Statement st = Db.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            double sum = 0;
            int count = 0;
            sb.append(String.format("%-40s %s%n", "ФИО", "Σ площадей"));
            sb.append("-".repeat(60)).append('\n');
            while (rs.next()) {
                String fio = rs.getString("FIO");
                double total = rs.getDouble("TOTAL");
                sb.append(String.format(Locale.US, "%-40s %.2f%n", fio, total));
                sum += total;
                count++;
            }
            double avg = count > 0 ? sum / count : 0;
            sb.append("-".repeat(60)).append('\n');
            sb.append(String.format(Locale.US,
                    "Средняя площадь на ответственного: %.2f кв.м (всего ответственных: %d)%n",
                    avg, count));
        } catch (SQLException e) {
            sb.append("Ошибка: ").append(e.getMessage());
        }
        outputArea.setText(sb.toString());
    }

    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Полностью очистить БД и заполнить значениями из seed-data.txt?\n" +
                        "ВСЕ текущие данные будут потеряны.",
                "Сброс БД", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Seed.resetDatabase();
            reloadResponsibles();
            reloadClassrooms();
            outputArea.setText("БД сброшена к значениям по умолчанию из файла seed-data.txt\n" +
                    "Транзакция: DELETE * 2 + INSERT * N в одной транзакции");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка сброса: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
