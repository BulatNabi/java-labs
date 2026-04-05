import java.io.*;

public class Logger {
    private PrintWriter pw;

    public void setJournalPath(String path) {
        try {
            pw = new PrintWriter(new FileWriter(path, false));
        } catch (IOException e) {
            System.out.println("Ошибка открытия журнала: " + e.getMessage());
        }
    }

    public void log(String msg) {
        System.out.println(msg);
        if (pw != null) {
            pw.println(msg);
            pw.flush();
        }
    }

    public void close() {
        if (pw != null) pw.close();
    }
}
