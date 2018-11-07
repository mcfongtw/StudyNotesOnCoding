package com.github.mcfongtw.jni.utils;

import com.github.fommil.jni.JniLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Set;


/*
 * XXX: Need to
 * export LD_LIBRARY_PATH=/tmp
 * Ref: http://javaagile.blogspot.com/2014/04/jni-and-ldlibrarypath.html
 *
 * Inspired from com.github.fommil.jniloader
 */
public class JniUtils {

    private static final Logger logger = LoggerFactory.getLogger(JniUtils.class);

    private static final Set<String> loadedFilePath = new HashSet<String>();

    private JniUtils() {
        // Avoid instantiations
    }

    public static void loadLibraryFromResourceStream(String libPath) {
        loadFromPath(libPath);
    }

    public static void loadLibraryFromFileSystem(String libPath) throws IOException {
        loadFromPath(libPath);
    }

    /**
     * Attempts to loadFromPath a native library from the {@code java.library.path}
     * and (if that fails) will extract the named file from the classpath
     * into a temporary directory is created) and loadFromPath from there.
     * <p/>
     * Will stop on the first successful loadFromPath of a native library.
     *
     * @param path alternative relative path of the native library
     *              on either the library path or classpath.
     * @throws ExceptionInInitializerError if the input parameters are invalid or
     *                                     all path failed to loadFromPath (making this
     *                                     safe to use in static code blocks).
     */
    private synchronized static void loadFromPath(String path) {
        String fileName = new File(path).getName();
        if (loadedFilePath.contains(fileName)) {
            logger.warn("[{}] already loaded. Skip!", path);
            return;
        }

        //1. Check if so files exists in LD_LIBRARY_PATH. If yes, loadFromPath it.
        String[] javaLibPath = System.getProperty("java.library.path").split(File.pathSeparator);
        for (String ldLibraryPath : javaLibPath) {
            File file = new File(ldLibraryPath, path).getAbsoluteFile();
            logger.debug("Searching {} for {}", ldLibraryPath, file.getName());
            if (file.exists() && file.isFile() && systemLoad(file, path))
                return;
        }

        //2. Extract the so files to /tmp/<temp-folder>/*.so and loadFromPath it.
        File extracted = extract(path);
        if (extracted != null && systemLoad(extracted, path))
            return;

        throw new ExceptionInInitializerError("unable to loadFromPath from " + path);
    }

    // return true if the file was loaded; false, otherwise.
    private static boolean systemLoad(File file, String filePath) {
        try {
            logger.debug("Attempting to loadFromPath shared library [{}]", file);
            System.load(file.getAbsolutePath());
            logger.info("Successfully loaded shared library [{}]", file);
            loadedFilePath.add(filePath);
            return true;
        } catch (UnsatisfiedLinkError e) {
            logger.warn("Failed to loadFromPath [{}]. Reason: [{}]", file, e.getMessage());
            return false;
        } catch (SecurityException e) {
            logger.warn("Failed to loadFromPath [{}]. Reason: [{}]", file, e.getMessage());
            return false;
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static File extract(String path) {
        try {
            long start = System.nanoTime();
            URL url = JniLoader.class.getResource("/" + path);
            if (url == null) {
                return null;
            }

            InputStream is = JniUtils.class.getResourceAsStream("/" + path);
            File tempFile = File.createTempFile("jni", path);
            tempFile.deleteOnExit();

            logger.info("Attempting to extract [{}] to [{}]", url, tempFile.getAbsoluteFile());

            //copy src to dst using transferFrom
            ReadableByteChannel src = Channels.newChannel(is);
            FileChannel dst = new FileOutputStream(tempFile).getChannel();
            dst.transferFrom(src, 0, Long.MAX_VALUE);

            long end = System.nanoTime();
            logger.info("Successfully extracted [{}] is [{}] millis", tempFile.getAbsolutePath(), ((end-start) / 1_000_000));

            return tempFile;
        } catch (Throwable e) {
            if (e instanceof SecurityException || e instanceof IOException) {
                logger.trace("Failed to extract [{}]. Reason: [{}]", path, e.getMessage());
                return null;
            } else throw new ExceptionInInitializerError(e);
        }
    }
}
