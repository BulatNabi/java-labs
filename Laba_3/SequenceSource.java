import java.util.ArrayList;

public class SequenceSource {
    private ArrayList<Integer> numbers = new ArrayList<>();
    private IVariableChangeEvent listener;

    public SequenceSource(IVariableChangeEvent listener) {
        this.listener = listener;
    }

    public void add(int n) {
        numbers.add(n);
        listener.onVariableChange("numbers", numbers);
    }

    public ArrayList<Integer> getOddIndexed() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            if ((i + 1) % 2 != 0) result.add(numbers.get(i));
        }
        return result;
    }

    public ArrayList<Integer> getEvenIndexed() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            if ((i + 1) % 2 == 0) result.add(numbers.get(i));
        }
        return result;
    }
}
