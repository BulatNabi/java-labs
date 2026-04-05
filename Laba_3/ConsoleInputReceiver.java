public class ConsoleInputReceiver implements IConsoleInputEvent {
    private Logger logger;

    public ConsoleInputReceiver(Logger logger) {
        this.logger = logger;
    }

    public void onConsoleInput(String prompt) {
        logger.log("[СОБЫТИЕ 4] Обращение к потоку ввода с консоли");
    }
}
