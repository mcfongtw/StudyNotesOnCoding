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
        public static void run(String[] cmds) throws IOException, InterruptedException
        {
            //      /* __debug_code__
            for(String cmd : cmds)
            {
                System.out.print(cmd);
                System.out.print(' ');
            }
            System.out.println();
            //      */
            Process process = Runtime.getRuntime().exec(cmds);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            while((line = input.readLine()) != null)
            {
                System.out.println(line);
            }
        }


        public static String[] buildCommands(String cmd, String sudoPasswd)
        {
            String[] cmds = {shellName, shellParam, "echo \"" + sudoPasswd + "\" | " + sudoCmd + " -S " + cmd};
            return cmds;
        }

        protected static String sudoCmd = "sudo";
        protected static String shellName = "/bin/bash";
        protected static String shellParam = "-c";

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        long startMillsTime = System.currentTimeMillis();
        //Set system clock 10 min forward
        SudoExecutor.run(SudoExecutor.buildCommands(DATE_COMMAND, PASSWORD));
        long stopMillisTime = System.currentTimeMillis();

        System.out.println("Monotonicity Test -> System.currentTimeMillis(): " + (stopMillisTime - startMillsTime) / MS_PER_SECOND + " s");

        long startNanoTime = System.nanoTime();
        //Set system clock 10 min forward
        SudoExecutor.run(SudoExecutor.buildCommands(DATE_COMMAND, PASSWORD));
        long stopNanoTime = System.nanoTime();

        System.out.println("Monotonicity Test -> System.nanoTime(): " + (stopNanoTime - startNanoTime) / NS_PER_SECOND + " s");
    }
}


