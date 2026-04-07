public class ValueTooSmallException extends Exception {
    private final int value;
    private final int minValue;

    public ValueTooSmallException(int value, int minValue) {
        this.value = value;
        this.minValue = minValue;
    }

    @Override
    public String toString() {
        return "ValueTooSmallException: значение " + value +
               " меньше допустимого минимума " + minValue;
    }
}
