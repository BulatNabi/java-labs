// Исключение 4: в массиве элементов больше указанного
public class TooManyElementsException extends Exception {
    private final int actual;
    private final int limit;

    public TooManyElementsException(int actual, int limit) {
        this.actual = actual;
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "TooManyElementsException: элементов в массиве " + actual +
               ", что превышает допустимый лимит " + limit;
    }
}
