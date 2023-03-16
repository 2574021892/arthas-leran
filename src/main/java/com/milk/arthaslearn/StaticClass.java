package com.milk.arthaslearn;

public class StaticClass {
    public static final String name = "Justin";
    public static String name2 = "Bieber";

    public static void changeName2(String name2) {
        StaticClass.name2 = name2;
    }

    public static void changeName3(String name2) {
        StaticClass.name2 = name2;
    }
}
