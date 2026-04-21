package engine;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import compute.Compute;
import compute.Task;

public class ComputeEngine implements Compute {

    public ComputeEngine() {
        super();
    }

    @Override
    public <T> T executeTask(Task<T> t) {
        System.out.println("[server] получена задача: " + t.getClass().getName());
        T result = t.execute();
        System.out.println("[server] результат: " + formatResult(result));
        return result;
    }

    private static String formatResult(Object r) {
        if (r instanceof int[][] a) {
            StringBuilder sb = new StringBuilder("int[][]{");
            for (int i = 0; i < a.length; i++) {
                sb.append(java.util.Arrays.toString(a[i]));
                if (i < a.length - 1) sb.append(", ");
            }
            return sb.append("}").toString();
        }
        return String.valueOf(r);
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        int registryPort = args.length > 0 ? Integer.parseInt(args[0]) : 1099;

        try {
            String name = "Compute";
            Compute engine = new ComputeEngine();
            Compute stub = (Compute) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry("localhost", registryPort);
            registry.rebind(name, stub);
            System.out.println("[server] ComputeEngine зарегистрирован как '" + name + "' в localhost:" + registryPort);
        } catch (Exception e) {
            System.err.println("[server] ошибка: " + e);
            e.printStackTrace();
        }
    }
}
