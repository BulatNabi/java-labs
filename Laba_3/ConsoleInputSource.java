import java.util.Scanner;

public class ConsoleInputSource {
    private IConsoleInputEvent listener;
    private Scanner scanner = new Scanner(System.in);

    public ConsoleInputSource(IConsoleInputEvent listener) {
        this.listener = listener;
    }

    public String read(String prompt) {
        listener.onConsoleInput(prompt);
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
