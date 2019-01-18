package com.github.mcfongtw;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import joptsimple.internal.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * java -Dlogback.configurationFile=poc/target/logback.xml -jar poc/target/LoggerConfWatcherDemo.jar
 */
public class LoggerConfWatcherDemo {

    private static Logger logger = LoggerFactory.getLogger(LoggerConfWatcherDemo.class);

    public static void main(String[] args) throws IOException {
        String logbackFilePath = System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY);

        if(Strings.isNullOrEmpty(logbackFilePath)) {
            printUsage();
            System.exit(-1);
        } else {
            logger.info("logback.xml defined at [{}]", logbackFilePath);


            FileModifiedObservable fileModifiedObservable = new FileModifiedObservable(new File(logbackFilePath));
            fileModifiedObservable.addObserver(new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    try {
                        //reload new logback config
                        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                        loggerContext.reset();
                        ContextInitializer contextInitializer = new ContextInitializer(loggerContext);
                        contextInitializer.autoConfig();

                        //update new logger instance
                        logger = loggerContext.getLogger(LoggerConfWatcherDemo.class);

                        //logger level test
                        logger.error("**ERROR**");
                        logger.warn("**WARN**");
                        logger.info("**INFO**");
                        logger.debug("**DEBUG**");
                        logger.trace("**TRACE**");
                    } catch (JoranException e) {
                        logger.error("Failed to update Observable: ", e);
                    }
                }
            });

        }
    }

    private static void printUsage() {
        logger.info("java -D{}=<path-to-logback.xml> -jar {}.jar", new Object[]{ContextInitializer.CONFIG_FILE_PROPERTY, LoggerConfWatcherDemo.class.getSimpleName()});
    }
}


class FileModifiedObservable extends Observable implements Runnable, Closeable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WatchService watcher = FileSystems.getDefault().newWatchService();

    private final Path pathToWatch;

    private final File fileToWatch;

    private AtomicInteger count = new AtomicInteger();

    private long lastModifiedTimestamp = 0;

    private boolean ignoreNext = false; // if we plan to modify the file ourselves, stay cool


    public FileModifiedObservable(File file) throws IOException {
        fileToWatch = file.getAbsoluteFile();
        pathToWatch = fileToWatch.toPath().getParent();

        try {
            pathToWatch.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            logger.info("Start watching [{}]", pathToWatch.getFileName().toAbsolutePath());
        } catch (IOException ioe) {
            logger.error("Failed to create FileModifiedObservable: " + file.getAbsolutePath(), ioe);
            return;
        }

        new Thread(this).start();
    }

    public void close() throws IOException {
        deleteObservers();
        watcher.close();
    }

    public void run() {

        WatchKey key;

        while(true) {
            try {
                key = watcher.take();
            } catch (InterruptedException err) {
                logger.error("Failed to watch file!", err);
                return;
            } catch (ClosedWatchServiceException err) {
                // This is expected to happen when we stop watching for a file.
                // With the return statement, the thread will stop.
                logger.error("Failed to watch file!", err);
                return;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                logger.trace("Event kind: [{}]. affected [{}].", new Object[]{event.kind(), event.context()});

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = pathToWatch.resolve(name);


                if (child.endsWith(fileToWatch.toPath())) {
                    logger.trace("File [{}] last modified time: [{}]", new Object[]{child, fileToWatch.lastModified()});
                    if (lastModifiedTimestamp < fileToWatch.lastModified()) {
                        //update lastModifiedTimestamp
                        lastModifiedTimestamp = fileToWatch.lastModified();
                        if (!doIgnoreNext()) {
                            setChanged();
                            notifyObservers(count.addAndGet(1));
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    continue;
                }
            }
        }
    }

    /**
     * Ignore the next event (for when we modify the file ourselves).
     */
    public void ignoreNext() {
        ignoreNext = true;
    }

    /* Returns whether to ignore the next event and toggle the ignore flag. */
    private boolean doIgnoreNext() {
        if (ignoreNext) {
            ignoreNext = false;
            return true;
        } else {
            return false;
        }
    }

    // Comes straight from the Oracle, must be good
    // see http://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public final File getFile() {
        return fileToWatch;
    }
}