package com.github.mcfongtw.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class SudoExecutors
{
    private SudoExecutors() {
        // avoid instantiation
    }

    private  static final String sudoCmd = "sudo";
    private static final String shellName = "/bin/bash";
    private static final String shellParam = "-c";

    public static void exec(String userCmd, String sudoPasswd) throws IOException, InterruptedException
    {
        String[] cmdStrings = {shellName, shellParam, "echo \"" + sudoPasswd + "\" | " + sudoCmd + " -S " + userCmd};

        for(String cmdStr : cmdStrings)
        {
            System.out.print(cmdStr);
            System.out.print(' ');
        }
        System.out.println();

        Process process = Runtime.getRuntime().exec(cmdStrings);
        InputStreamReader ir = new InputStreamReader(process.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        String line;
        while((line = input.readLine()) != null)
        {
            System.out.println(line);
        }
    }





}