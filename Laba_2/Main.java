public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Введите числа через пробел в аргументах командной строки.");
            return;
        }

        NumberSequence seq = new NumberSequence();

        try {
            seq.load(args);
            if (seq.size() == 0) return;
            seq.validateSize(10);
            seq.validateMinValue(0);
            System.out.println("Нечётные номера: " + seq.getOddIndexed());
            System.out.println("Чётные номера: " + seq.getEvenIndexed());
        } catch (StringContainsLettersException e) {
            System.out.println(e);
        } catch (TooManyElementsException e) {
            System.out.println(e);
        } catch (ValueTooSmallException e) {
            System.out.println(e);
        }
    }
}
