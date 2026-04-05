import java.util.ArrayList;

public interface IVariableChangeEvent {
    void onVariableChange(String varName, ArrayList<Integer> newValue);
}
