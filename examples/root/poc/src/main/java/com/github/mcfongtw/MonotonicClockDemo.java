package com.github.mcfongtw;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class MonotonicClockDemo {

    public static final long MS_PER_SECOND = 1000;

    public static final long NS_PER_SECOND = 1000 * 1000 * 1000;

    public static final String DATE_COMMAND = "date -s \"1 min\"";

    public static final String PASSWORD = "Love_0502";

    private static class SudoExecutor
    {
        public static void run(String userCmd, String sudoPasswd) throws IOException, InterruptedException
        {
            String[] cmdStrings = {shellName, shellParam, "echo \"" + sudoPasswd + "\" | " + sudoCmd + " -S " + userCmd};

            for(String cmdStr : cmdStrings)
            {
                System.out.print(cmdStr);
                System.out.print(' ');
            }
            System.out.println();
            //      */
            Process process = Runtime.getRuntime().exec(cmdStrings);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            while((line = input.readLine()) != null)
            {
                System.out.println(line);
            }
        }



        protected static String sudoCmd = "sudo";
        protected static String shellName = "/bin/bash";
        protected static String shellParam = "-c";

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        long startMillsTime = System.currentTimeMillis();
        //Set system clock 10 min forward
        SudoExecutor.run(DATE_COMMAND, PASSWORD);
        long stopMillisTime = System.currentTimeMillis();

        System.out.println("Monotonicity Test -> System.currentTimeMillis(): " + (stopMillisTime - startMillsTime) / MS_PER_SECOND + " s");

        long startNanoTime = System.nanoTime();
        //Set system clock 10 min forward
        SudoExecutor.run(DATE_COMMAND, PASSWORD);
        long stopNanoTime = System.nanoTime();

        System.out.println("Monotonicity Test -> System.nanoTime(): " + (stopNanoTime - startNanoTime) / NS_PER_SECOND + " s");
    }
}


