import java.io.*;
import java.util.*;

public class Laba3 {
    public static void main(String[] args) {
        Logger logger = new Logger();

        ConsoleInputReceiver consoleReceiver = new ConsoleInputReceiver(logger);
        FileInputReceiver fileReceiver = new FileInputReceiver(logger);
        VariableChangeReceiver varReceiver = new VariableChangeReceiver(logger);

        ConsoleInputSource consoleSource = new ConsoleInputSource(consoleReceiver);
        FileInputSource fileSource = new FileInputSource(fileReceiver);
        SequenceSource sequence = new SequenceSource(varReceiver);

        try {
            String filePath = consoleSource.read("Введите путь к файлу с данными: ");

            List<String> lines = fileSource.readLines(filePath);

            if (lines.size() < 2) {
                System.out.println("Файл должен содержать минимум 2 строки: путь к журналу и числа.");
                return;
            }

            String journalPath = lines.get(0).trim();
            logger.setJournalPath(journalPath);
            logger.log("Журнал: " + journalPath);

            String[] tokens = lines.get(1).trim().split("\\s+");
            for (String token : tokens) {
                try {
                    int n = Integer.parseInt(token);
                    sequence.add(n);
                } catch (NumberFormatException e) {
                    logger.log("Ошибка: \"" + token + "\" не является целым числом.");
                    return;
                }
            }

            logger.log("\nРезультат:");
            logger.log("Нечётные номера: " + sequence.getOddIndexed());
            logger.log("Чётные номера: " + sequence.getEvenIndexed());

        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        } finally {
            logger.close();
        }
    }
}
