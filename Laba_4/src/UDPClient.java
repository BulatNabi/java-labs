import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class UDPClient {
    private static final int BUFFER_SIZE = 4096;
    private static final int TIMEOUT_MS = 5000;
    private static final String CONFIG_PATH = "config/client.properties";

    private final DatagramSocket socket;
    private final InetAddress serverAddr;
    private final int serverPort;
    private final PrintWriter logWriter;

    public UDPClient(String host, int port, String logPath) throws IOException {
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(TIMEOUT_MS);
        this.serverAddr = InetAddress.getByName(host);
        this.serverPort = port;
        this.logWriter = new PrintWriter(new FileWriter(logPath, true), true);
    }

    private String exchange(String msg) throws IOException {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(data, data.length, serverAddr, serverPort));

        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket in = new DatagramPacket(buf, buf.length);
        socket.receive(in);
        String response = new String(in.getData(), 0, in.getLength(), StandardCharsets.UTF_8).trim();
        log("FROM server: " + response);
        return response;
    }

    private void log(String line) {
        String stamped = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + line;
        logWriter.println(stamped);
    }

    private void waitEnter(BufferedReader in) throws IOException {
        System.out.print("Нажмите Enter для отправки 2-й фазы...");
        in.readLine();
    }

    public void menu(BufferedReader in) throws IOException {
        while (true) {
            System.out.println("\n--- Меню ---");
            System.out.println("1. Размерность массива");
            System.out.println("2. Прочитать ячейку");
            System.out.println("3. Записать ячейку");
            System.out.println("4. Batch: несколько ячеек -> дефолт");
            System.out.println("0. Выход");
            System.out.print("Выбор: ");
            String choice = in.readLine();
            if (choice == null) return;
            try {
                switch (choice.trim()) {
                    case "1" -> opGetDim(in);
                    case "2" -> opRead(in);
                    case "3" -> opWrite(in);
                    case "4" -> opBatchSet(in);
                    case "0" -> { return; }
                    default -> System.out.println("Неизвестный пункт");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void opGetDim(BufferedReader in) throws IOException {
        String r1 = exchange("CMD GET_DIM");
        System.out.println("Сервер: " + r1);
        if (!r1.startsWith("OK")) return;
        System.out.print("Индекс массива (0=int, 1=double, 2=string): ");
        String arr = in.readLine().trim();
        String r2 = exchange("ARR " + arr);
        System.out.println("Сервер: " + r2);
    }

    private void opRead(BufferedReader in) throws IOException {
        System.out.print("Индекс массива: ");
        String arr = in.readLine().trim();
        String r1 = exchange("CMD READ " + arr);
        System.out.println("Сервер: " + r1);
        if (!r1.startsWith("OK")) return;
        System.out.print("Строка i: ");
        String i = in.readLine().trim();
        System.out.print("Столбец j: ");
        String j = in.readLine().trim();
        String r2 = exchange("IDX " + i + " " + j);
        System.out.println("Сервер: " + r2);
    }

    private void opWrite(BufferedReader in) throws IOException {
        System.out.print("Индекс массива: ");
        String arr = in.readLine().trim();
        System.out.print("Строка i: ");
        String i = in.readLine().trim();
        System.out.print("Столбец j: ");
        String j = in.readLine().trim();
        String r1 = exchange("CMD WRITE " + arr + " " + i + " " + j);
        System.out.println("Сервер: " + r1);
        if (!r1.startsWith("OK")) return;
        System.out.print("Значение: ");
        String val = in.readLine();
        String r2 = exchange("VAL " + val);
        System.out.println("Сервер: " + r2);
    }

    private void opBatchSet(BufferedReader in) throws IOException {
        System.out.println("Формат: a,i,j;a,i,j;... (например 0,1,2;2,3,3)\n");
        System.out.println("Введите кол-во изменяемых ячеек: ");
        String count = in.readLine().trim();
        String r1 = exchange("CMD BATCH_SET " + count);
        System.out.println("Сервер: " + r1);
        if (!r1.startsWith("OK")) return;
        System.out.print("Ячейки: ");
        String cells = in.readLine().trim();
        String r2 = exchange("CELLS " + cells);
        System.out.println("Сервер: " + r2);
    }

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(CONFIG_PATH)) {
            props.load(is);
        }
        String host = props.getProperty("server.host");
        int port = Integer.parseInt(props.getProperty("server.port"));

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.print("Введите путь к файлу журнала клиента: ");
        String logPath = in.readLine().trim();

        System.out.println("Подключение к " + host + ":" + port);
        UDPClient client = new UDPClient(host, port, logPath);
        client.menu(in);
    }
}
