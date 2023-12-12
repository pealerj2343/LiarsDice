package com.example.liarsdice;

import java.util.Random;

public class Dice {
    private int number;

    public Dice() {
        roll();
    }

    public void roll() {
        Random random = new Random();
        number = (int) ((random.nextInt(6)) + 1);
    }

    public Dice(int num) {
        number = num;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "Dice value: " + number;
    }
}

