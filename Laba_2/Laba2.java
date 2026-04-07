import java.util.*;
public class Laba2 {
    public static void main(String[] args) {
        NumberSequence seq = new NumberSequence();

        String[] test1 = {"10", "abc", "30"};
        System.out.println("=== StringContainsLettersException ===");
        System.out.println("Аргументы: " + Arrays.toString(test1));
        try {
            seq.load(test1);
        } catch (StringContainsLettersException e) {
            System.out.println(e);
        }

        String[] test2 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
        System.out.println("\n=== TooManyElementsException ===");
        System.out.println("Аргументы: " + Arrays.toString(test2));
        try {
            seq.load(test2);
            seq.validateSize(10);
        } catch (StringContainsLettersException e) {
            System.out.println(e);
        } catch (TooManyElementsException e) {
            System.out.println(e);
        }

        String[] test3 = {"5", "-3", "10"};
        System.out.println("\n=== ValueTooSmallException ===");
        System.out.println("Аргументы: " + Arrays.toString(test3));
        try {
            seq.load(test3);
            seq.validateMinValue(0);
        } catch (StringContainsLettersException e) {
            System.out.println(e);
        } catch (ValueTooSmallException e) {
            System.out.println(e);
        }

        System.out.println("\n=== Основная работа ===");

        if (args.length == 0) {
            System.out.println("Введите числа через пробел в аргументах командной строки.");
            return;
        }

        try {
            seq.load(args);
            if (seq.size() == 0) return;
            seq.validateSize(10);
            seq.validateMinValue(0);
            System.out.println("Нечётные номера: " + seq.getOddIndexed());
            System.out.println("Чётные номера:   " + seq.getEvenIndexed());
        } catch (StringContainsLettersException e) {
            System.out.println(e);
        } catch (TooManyElementsException e) {
            System.out.println(e);
        } catch (ValueTooSmallException e) {
            System.out.println(e);
        }
    }
}
