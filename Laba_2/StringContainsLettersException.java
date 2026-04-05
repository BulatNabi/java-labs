// Исключение 9: строка содержит буквы (не является числом)
public class StringContainsLettersException extends Exception {
    private final String input;

    public StringContainsLettersException(String input) {
        this.input = input;
    }

    @Override
    public String toString() {
        return "StringContainsLettersException: строка аргументов содержит буквы и не может быть преобразована в число";
    }
}
