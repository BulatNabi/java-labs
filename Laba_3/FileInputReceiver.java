public class FileInputReceiver implements IFileInputEvent {
    private Logger logger;

    public FileInputReceiver(Logger logger) {
        this.logger = logger;
    }

    public void onFileInput(String filename) {
        logger.log("[СОБЫТИЕ 9] Обращение к потоку ввода из файла: " + filename);
    }
}
