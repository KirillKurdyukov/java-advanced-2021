package info.kgeorgiy.ja.kurdyukov.arrayset;

import java.util.*;


public class Main {

    public static void main(String [] args) {
        ArraySet<Integer> arraySet = new ArraySet<>(List.of(1, 2, 3));
        ArrayList<Integer> a = new ArrayList<>();
        System.out.println(arraySet.descendingSet().descendingSet());
    }
}
