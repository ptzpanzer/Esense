package com.lcf.esensedial;

import java.util.ArrayList;
import java.util.Arrays;

public class Dial {
    private String number;
    private ArrayList<String> acts;

    public Dial(String line) {
        acts = new ArrayList<>();

        String[] temp = line.split(" ");
        number = temp[0];
        acts.addAll(Arrays.asList(temp).subList(1, temp.length));
    }

    public String getNumber() {
        return number;
    }

    public ArrayList<String> getActs() {
        return acts;
    }
}
