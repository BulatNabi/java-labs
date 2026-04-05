import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Введите числа через пробел в аргументах командной строки.");
            return;
        }

        ArrayList<String> odd = new ArrayList<>(); 
        ArrayList<String> even = new ArrayList<>();  

        for (int i = 0; i < args.length; i++) {
            try {
                Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: \"" + args[i] + "\" не является целым числом.");
                return;
            }
            if ((i + 1) % 2 != 0) {
                odd.add(args[i]);
            } else {
                even.add(args[i]);
            }
        }

        System.out.println("Последовательность с нечётными номерами: " + odd);
        System.out.println("Последовательность с чётными номерами: " + even);
    }
}
