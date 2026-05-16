package jspappl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Calculator implements Serializable {
    private String fio;
    private String input;
    private List<String> oddPositions = new ArrayList<>();
    private List<String> evenPositions = new ArrayList<>();
    private List<String> bad = new ArrayList<>();

    public Calculator() {
        fio = "";
        input = "";
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
        compute();
    }

    public List<String> getOddPositions() {
        return oddPositions;
    }

    public List<String> getEvenPositions() {
        return evenPositions;
    }

    public List<String> getBad() {
        return bad;
    }

    public void compute() {
        oddPositions = new ArrayList<>();
        evenPositions = new ArrayList<>();
        bad = new ArrayList<>();
        if (input == null || input.isEmpty()) return;

        String[] tokens = input.split("[,\\s]+");
        int pos = 1;
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            try {
                Integer.parseInt(token);
            } catch (NumberFormatException ex) {
                bad.add(token);
                continue;
            }
            if (pos % 2 != 0) {
                oddPositions.add(token);
            } else {
                evenPositions.add(token);
            }
            pos++;
        }
    }
}
