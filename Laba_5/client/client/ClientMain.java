package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import compute.Compute;

public class ClientMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: ClientMain <registryHost> <num1> <num2> ...");
            System.exit(1);
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        String registryHost = args[0];
        int[] numbers = new int[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            try {
                numbers[i - 1] = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: \"" + args[i] + "\" не является целым числом.");
                System.exit(2);
            }
        }

        try {
            String name = "Compute";
            Registry registry = LocateRegistry.getRegistry(registryHost);
            Compute comp = (Compute) registry.lookup(name);

            SplitTask task = new SplitTask(numbers);
            int[][] result = comp.executeTask(task);

            System.out.println("Исходный массив: " + Arrays.toString(numbers));
            System.out.println("Последовательность с нечётными номерами: " + Arrays.toString(result[0]));
            System.out.println("Последовательность с чётными номерами:  " + Arrays.toString(result[1]));
        } catch (Exception e) {
            System.err.println("ClientMain exception: " + e);
            e.printStackTrace();
        }
    }
}
