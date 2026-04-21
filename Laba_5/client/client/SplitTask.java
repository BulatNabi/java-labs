package client;

import compute.Task;
import java.util.ArrayList;

public class SplitTask implements Task<int[][]> {
    private static final long serialVersionUID = 1L;

    private final int[] numbers;

    public SplitTask(int[] numbers) {
        this.numbers = numbers;
    }

    @Override
    public int[][] execute() {
        ArrayList<Integer> odd = new ArrayList<>();
        ArrayList<Integer> even = new ArrayList<>();
        for (int i = 0; i < numbers.length; i++) {
            if ((i + 1) % 2 != 0) odd.add(numbers[i]);
            else even.add(numbers[i]);
        }
        return new int[][] { toArray(odd), toArray(even) };
    }

    private static int[] toArray(ArrayList<Integer> list) {
        int[] a = new int[list.size()];
        for (int i = 0; i < list.size(); i++) a[i] = list.get(i);
        return a;
    }
}
