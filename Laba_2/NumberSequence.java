import java.util.ArrayList;

public class NumberSequence implements ISequenceOps, IValidator {
    private final ArrayList<Integer> numbers = new ArrayList<>();

    @Override
    public void load(String[] args) throws StringContainsLettersException {
        numbers.clear();
        for (String arg : args) {
            if (arg.matches(".*[a-zA-Zа-яА-Я].*")) {
                throw new StringContainsLettersException(arg);
            }
            try {
                numbers.add(Integer.parseInt(arg));
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: \"" + arg + "\" не является целым числом.");
                numbers.clear();
                return;
            }
        }
    }

    @Override
    public ArrayList<Integer> getOddIndexed() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            if ((i + 1) % 2 != 0) {
                result.add(numbers.get(i));
            }
        }
        return result;
    }

    @Override
    public ArrayList<Integer> getEvenIndexed() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            if ((i + 1) % 2 == 0) {
                result.add(numbers.get(i));
            }
        }
        return result;
    }

    @Override
    public void validateSize(int maxSize) throws TooManyElementsException {
        if (numbers.size() > maxSize) {
            throw new TooManyElementsException(numbers.size(), maxSize);
        }
    }

    @Override
    public void validateMinValue(int minValue) throws ValueTooSmallException {
        for (int n : numbers) {
            if (n < minValue) {
                throw new ValueTooSmallException(n, minValue);
            }
        }
    }

    public int size() {
        return numbers.size();
    }
}
