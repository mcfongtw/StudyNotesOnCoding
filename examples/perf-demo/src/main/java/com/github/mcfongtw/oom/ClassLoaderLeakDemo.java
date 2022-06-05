package com.github.mcfongtw.oom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;


/*
 * Inspired by https://gist.github.com/dpryden/b2bb29ee2d146901b4ae
 */
public class ClassLoaderLeakDemo {

    static volatile boolean running = true;

    public static String targetClassPath;

    static {
        targetClassPath = "";
    }

    public static void main(String[] args) throws Exception {

        if(args.length < 1) {
            System.out.println("java ClassLoaderLeakDemo '<Target class path>'");
            System.exit(-1);
        }

        targetClassPath = args[0];
        System.out.println("Target Class Path: [" + targetClassPath + "]");

        Thread thread = new LongRunningThread();
        try {
            thread.start();
            System.out.println("Running, press any key to stop.");
            System.in.read();
        } finally {
            running = false;
            thread.join();
        }
    }

    /**
     * Implementation of the thread. It just calls {@link #loadAndDiscard()}
     * in a loop.
     */
    static final class LongRunningThread extends Thread {


        @Override public void run() {
            while(running) {
                try {
                    loadAndDiscard();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.out.println("Caught InterruptedException, shutting down.");
                    running = false;
                }
            }
        }
    }

    /**
     * A simple ClassLoader implementation that is only able to load one
     * class, the LoadedInChildClassLoader class. We have to jump through
     * some hoops here because we explicitly want to ensure we get a new
     * class each time (instead of reusing the class loaded by the system
     * class loader). If this child class were in a JAR file that wasn't
     * part of the system classpath, we wouldn't need this mechanism.
     */
    static final class ChildOnlyClassLoader extends ClassLoader {
        ChildOnlyClassLoader() {
            super(ClassLoaderLeakDemo.class.getClassLoader());
        }

        @Override protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            if (!LoadedInChildClassLoader.class.getName().equals(name)) {
                return super.loadClass(name, resolve);
            }
            try {
                Path path = Paths.get(targetClassPath + LoadedInChildClassLoader.class.getName().replace('.', '/')
                        + ".class");
                byte[] classBytes = Files.readAllBytes(path);
                Class<?> c = defineClass(name, classBytes, 0, classBytes.length);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            } catch (IOException ex) {
                throw new ClassNotFoundException("Could not load " + name, ex);
            }
        }
    }

    /**
     * Helper method that constructs a new ClassLoader, loads a single class,
     * and then discards any reference to them. Theoretically, there should
     * be no GC impact, since no references can escape this method! But in
     * practice this will leak memory like a sieve.
     */
    static void loadAndDiscard() throws Exception {
        ClassLoader childClassLoader = new ChildOnlyClassLoader();
        Class<?> childClass = Class.forName(
                LoadedInChildClassLoader.class.getName(), true, childClassLoader);
        childClass.newInstance();
        // When this method returns, there will be no way to reference
        // childClassLoader or childClass at all, but they will still be
        // rooted for GC purposes!
    }

    /**
     * An innocuous-looking class. Doesn't do anything interesting.
     */
    public static final class LoadedInChildClassLoader {
        // Grab a bunch of bytes. This isn't necessary for the leak, it just
        // makes the effect visible more quickly.
        // Note that we're really leaking these bytes, since we're effectively
        // creating a new instance of this static final field on each iteration!
        static final byte[] moreBytesToLeak = new byte[1024 * 1024 * 10];

        private static final ThreadLocal<LoadedInChildClassLoader> threadLocal
                = new ThreadLocal<>();

        public LoadedInChildClassLoader() {
            // Stash a reference to this class in the ThreadLocal
            threadLocal.set(this);
        }
    }
}
