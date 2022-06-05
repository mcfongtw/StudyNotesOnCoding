package com.github.mcfongtw.oom;

import java.io.BufferedReader;
import java.io.FileReader;

public class UnclosedOpenFileStreamDemo {

    public static String targetClassPath;

    static {
        targetClassPath = "";
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.out.println("java UnclosedOpenFileStreamDemo '<Target class path>'");
            System.exit(-1);
        }

        targetClassPath = args[0];
        System.out.println("Target Class Path: [" + targetClassPath + "]");

        String inputFile = targetClassPath + UnclosedOpenFileStreamDemo.class.getName().replace('.', '/')  + ".class";
        while(true) {
            try {
                new BufferedReader(new FileReader(inputFile));


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
