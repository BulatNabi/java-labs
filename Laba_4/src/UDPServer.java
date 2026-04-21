import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UDPServer {
    private static final int ROWS = 5;
    private static final int COLS = 5;
    private static final int BUFFER_SIZE = 4096;

    private static final int DEFAULT_INT = 0;
    private static final double DEFAULT_DOUBLE = 0.0;
    private static final String DEFAULT_STRING = "";

    private final int[][] intArr = new int[ROWS][COLS];
    private final double[][] doubleArr = new double[ROWS][COLS];
    private final String[][] stringArr = new String[ROWS][COLS];

    private final Map<String, PendingCommand> sessions = new HashMap<>();

    private final DatagramSocket socket;
    private final PrintWriter logWriter;

    public UDPServer(int port, String logPath) throws IOException {
        this.socket = new DatagramSocket(port);
        this.logWriter = new PrintWriter(new FileWriter(logPath, true), true);
        for (int i = 0; i < ROWS; i++) Arrays.fill(stringArr[i], "");
        System.out.println("Сервер запущен на порту " + port);
        System.out.println("Журнал сервера: " + logPath);
        printArrays();
    }

    public void run() throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
            String sessionKey = packet.getAddress().getHostAddress() + ":" + packet.getPort();
            log("FROM " + sessionKey + ": " + msg);

            String response;
            try {
                response = handle(sessionKey, msg);
            } catch (Exception e) {
                sessions.remove(sessionKey);
                response = "ERR " + e.getMessage();
            }
            sendResponse(packet.getAddress(), packet.getPort(), response);
        }
    }

    private String handle(String sessionKey, String msg) {
        PendingCommand pending = sessions.get(sessionKey);
        if (pending == null) return handlePhase1(sessionKey, msg);
        sessions.remove(sessionKey);
        return handlePhase2(pending, msg);
    }

    private String handlePhase1(String sessionKey, String msg) {
        String[] parts = msg.split("\\s+");
        if (parts.length < 2 || !parts[0].equals("CMD")) return "ERR expected 'CMD <op> ...'";
        String cmd = parts[1];
        String[] args = Arrays.copyOfRange(parts, 2, parts.length);

        switch (cmd) {
            case "GET_DIM":
                sessions.put(sessionKey, new PendingCommand(cmd, args));
                return "OK send 'ARR <idx>'";
            case "READ":
                if (args.length != 1) return "ERR usage: CMD READ <arrIdx>";
                sessions.put(sessionKey, new PendingCommand(cmd, args));
                return "OK send 'IDX <i> <j>'";
            case "WRITE":
                if (args.length != 3) return "ERR usage: CMD WRITE <arrIdx> <i> <j>";
                sessions.put(sessionKey, new PendingCommand(cmd, args));
                return "OK send 'VAL <value>'";
            case "BATCH_SET":
                if (args.length != 1) return "ERR usage: CMD BATCH_SET <count>";
                sessions.put(sessionKey, new PendingCommand(cmd, args));
                return "OK send 'CELLS a,i,j;a,i,j;...'";
            default:
                return "ERR unknown command: " + cmd;
        }
    }

    private String handlePhase2(PendingCommand pending, String msg) {
        switch (pending.cmd) {
            case "GET_DIM": {
                String[] parts = msg.split("\\s+");
                if (parts.length != 2 || !parts[0].equals("ARR")) return "ERR expected 'ARR <idx>'";
                int arr = Integer.parseInt(parts[1]);
                validateArrayIndex(arr);
                return "DIM " + ROWS + " " + COLS;
            }
            case "READ": {
                int arr = Integer.parseInt(pending.args[0]);
                validateArrayIndex(arr);
                String[] parts = msg.split("\\s+");
                if (parts.length != 3 || !parts[0].equals("IDX")) return "ERR expected 'IDX <i> <j>'";
                int i = Integer.parseInt(parts[1]);
                int j = Integer.parseInt(parts[2]);
                validateCell(i, j);
                String val = switch (arr) {
                    case 0 -> String.valueOf(intArr[i][j]);
                    case 1 -> String.valueOf(doubleArr[i][j]);
                    case 2 -> stringArr[i][j];
                    default -> throw new IllegalStateException();
                };
                return "VAL " + val;
            }
            case "WRITE": {
                int arr = Integer.parseInt(pending.args[0]);
                int i = Integer.parseInt(pending.args[1]);
                int j = Integer.parseInt(pending.args[2]);
                validateArrayIndex(arr);
                validateCell(i, j);
                if (!msg.startsWith("VAL ") && !msg.equals("VAL")) return "ERR expected 'VAL <value>'";
                String value = msg.length() > 4 ? msg.substring(4) : "";
                switch (arr) {
                    case 0 -> intArr[i][j] = Integer.parseInt(value);
                    case 1 -> doubleArr[i][j] = Double.parseDouble(value);
                    case 2 -> stringArr[i][j] = value;
                }
                printArrays();
                return "OK written";
            }
            case "BATCH_SET": {
                int expectedCount = Integer.parseInt(pending.args[0]);
                if (!msg.startsWith("CELLS ")) return "ERR expected 'CELLS ...'";
                String cellsStr = msg.substring(6).trim();
                String[] cells = cellsStr.split(";");
                if (cells.length != expectedCount) return "ERR cell count mismatch: expected " + expectedCount + ", got " + cells.length;
                for (String cell : cells) {
                    String[] triple = cell.split(",");
                    if (triple.length != 3) return "ERR bad cell spec: " + cell;
                    int arr = Integer.parseInt(triple[0].trim());
                    int i = Integer.parseInt(triple[1].trim());
                    int j = Integer.parseInt(triple[2].trim());
                    validateArrayIndex(arr);
                    validateCell(i, j);
                    switch (arr) {
                        case 0 -> intArr[i][j] = DEFAULT_INT;
                        case 1 -> doubleArr[i][j] = DEFAULT_DOUBLE;
                        case 2 -> stringArr[i][j] = DEFAULT_STRING;
                    }
                }
                printArrays();
                return "OK set " + cells.length + " cells to defaults";
            }
            default:
                return "ERR unexpected state";
        }
    }

    private void validateArrayIndex(int arr) {
        if (arr < 0 || arr > 2) throw new IllegalArgumentException("array index must be 0..2");
    }

    private void validateCell(int i, int j) {
        if (i < 0 || i >= ROWS || j < 0 || j >= COLS)
            throw new IllegalArgumentException("cell out of bounds: " + i + "," + j);
    }

    private void sendResponse(InetAddress addr, int port, String response) throws IOException {
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        DatagramPacket out = new DatagramPacket(data, data.length, addr, port);
        socket.send(out);
    }

    private void log(String line) {
        String stamped = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + line;
        logWriter.println(stamped);
    }

    private void printArrays() {
        System.out.println("--- int ---");
        for (int[] row : intArr) System.out.println(Arrays.toString(row));
        System.out.println("--- double ---");
        for (double[] row : doubleArr) System.out.println(Arrays.toString(row));
        System.out.println("--- string ---");
        for (String[] row : stringArr) System.out.println(Arrays.toString(row));
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java UDPServer <port> <logPath>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String logPath = args[1];
        new UDPServer(port, logPath).run();
    }

    private record PendingCommand(String cmd, String[] args) {}
}
