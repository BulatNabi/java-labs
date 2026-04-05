import java.util.ArrayList;

public class VariableChangeReceiver implements IVariableChangeEvent {
    private Logger logger;

    public VariableChangeReceiver(Logger logger) {
        this.logger = logger;
    }

    public void onVariableChange(String varName, ArrayList<Integer> newValue) {
        logger.log("[СОБЫТИЕ 5] Изменение переменной '" + varName + "': " + newValue);
    }
}
