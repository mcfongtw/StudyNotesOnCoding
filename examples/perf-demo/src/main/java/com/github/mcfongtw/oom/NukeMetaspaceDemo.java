package com.github.mcfongtw.oom;

public class NukeMetaspaceDemo {

    static javassist.ClassPool cp = javassist.ClassPool.getDefault();

    //-XX:MaxMetaspaceSize=64m
    public static void main(String[] args) throws Exception{
        for (int i = 0; ; i++) {
            Class c = cp.makeClass("com.github.mcfongtw.oom.demo.Generated" + i).toClass();
        }
    }
}
