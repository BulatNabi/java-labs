package univapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Seed {
    private static final String FILE = "seed-data.txt";

    public static class Responsible {
        public final int id;
        public final String fio, position, phone;
        public final int age;
        public Responsible(int id, String fio, String position, String phone, int age) {
            this.id = id; this.fio = fio; this.position = position;
            this.phone = phone; this.age = age;
        }
    }

    public static class Classroom {
        public final int id;
        public final String building, roomNumber, name;
        public final double area;
        public final int responsibleId;
        public Classroom(int id, String building, String roomNumber, String name,
                         double area, int responsibleId) {
            this.id = id; this.building = building; this.roomNumber = roomNumber;
            this.name = name; this.area = area; this.responsibleId = responsibleId;
        }
    }

    public static class Data {
        public final List<Responsible> responsibles = new ArrayList<>();
        public final List<Classroom> classrooms = new ArrayList<>();
    }

    public static Data parse() throws IOException {
        Data data = new Data();
        Path p = Path.of(FILE);
        if (!Files.exists(p)) {
            throw new IOException("Файл " + FILE + " не найден");
        }

        String section = null;
        for (String raw : Files.readAllLines(p, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1);
                continue;
            }
            String[] f = line.split("\\|");
            if ("RESPONSIBLES".equals(section) && f.length == 5) {
                data.responsibles.add(new Responsible(
                        Integer.parseInt(f[0].trim()),
                        f[1].trim(), f[2].trim(), f[3].trim(),
                        Integer.parseInt(f[4].trim())));
            } else if ("CLASSROOMS".equals(section) && f.length == 6) {
                data.classrooms.add(new Classroom(
                        Integer.parseInt(f[0].trim()),
                        f[1].trim(), f[2].trim(), f[3].trim(),
                        Double.parseDouble(f[4].trim()),
                        Integer.parseInt(f[5].trim())));
            }
        }
        return data;
    }

    public static void resetDatabase() throws SQLException, IOException {
        Data data = parse();
        Connection c = Db.get();
        c.setAutoCommit(false);
        try (Statement stmt = c.createStatement()) {
            stmt.executeUpdate("DELETE FROM CLASSROOMS");
            stmt.executeUpdate("DELETE FROM RESPONSIBLES");

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO RESPONSIBLES (ID, FIO, POSITION, PHONE, AGE) VALUES (?,?,?,?,?)")) {
                for (Responsible r : data.responsibles) {
                    ps.setInt(1, r.id);
                    ps.setString(2, r.fio);
                    ps.setString(3, r.position);
                    ps.setString(4, r.phone);
                    ps.setInt(5, r.age);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO CLASSROOMS (ID, BUILDING, ROOM_NUMBER, NAME, AREA, RESPONSIBLE_ID) " +
                            "VALUES (?,?,?,?,?,?)")) {
                for (Classroom cr : data.classrooms) {
                    ps.setInt(1, cr.id);
                    ps.setString(2, cr.building);
                    ps.setString(3, cr.roomNumber);
                    ps.setString(4, cr.name);
                    ps.setDouble(5, cr.area);
                    ps.setInt(6, cr.responsibleId);
                    ps.executeUpdate();
                }
            }

            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
    }
}
