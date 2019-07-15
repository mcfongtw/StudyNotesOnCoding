package com.github.mcfongtw.io;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import joptsimple.internal.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LoggerConfWatcherDemo {

    private static Logger logger = LoggerFactory.getLogger(LoggerConfWatcherDemo.class);

    static class LoggerConfModifiedObserver implements FileModifiedObserver {

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

    }

    public static void main(String[] args) throws IOException {
        String logbackFilePath = System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY);

        if(Strings.isNullOrEmpty(logbackFilePath)) {
            printUsage();
            System.exit(-1);
        } else {
            logger.info("logback.xml defined at [{}]", logbackFilePath);


            FileChangeService fileChangeService = new FileChangeService(new File(logbackFilePath), new LoggerConfModifiedObserver());
            new Thread(fileChangeService).start();

        }
    }

    private static void printUsage() {
        log.info("java -D{}=<path-to-logback.xml> -jar {}.jar", new Object[]{ContextInitializer.CONFIG_FILE_PROPERTY, LoggerConfWatcherDemo.class.getSimpleName()});
    }
}

interface FileModifiedObserver extends Observer {
}

@Slf4j
class FileChangeService extends Observable implements Runnable, Closeable {

    private final WatchService watcher = FileSystems.getDefault().newWatchService();

    // internal member
    private final Path dirPathToWatch;

    @Getter
    private final File fileToWatch;

    private AtomicInteger modifiedCount = new AtomicInteger();

    @Getter
    private long lastModifiedTimestamp = 0;


    public FileChangeService(File file, FileModifiedObserver observer) throws IOException {
        fileToWatch = file.getAbsoluteFile();
        dirPathToWatch = fileToWatch.getParentFile().toPath();

        try {
            dirPathToWatch.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            log.info("Start watching [{}]", dirPathToWatch.normalize().toAbsolutePath());
        } catch (IOException ioe) {
            log.error("Failed to create FileChangeService: " + file.getAbsolutePath(), ioe);
            return;
        }

        //add observer
        this.addObserver(observer);
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
                log.error("Failed to watch file!", err);
                return;
            } catch (ClosedWatchServiceException err) {
                // This is expected to happen when we stop watching for a file.
                // With the return statement, the thread will stop.
                log.warn("STOP to watch file!", err);
                return;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                log.debug("Event kind: [{}]. affected [{}].", new Object[]{event.kind(), event.context()});

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dirPathToWatch.resolve(name);


                if (child.endsWith(fileToWatch.toPath())) {
                    log.debug("File [{}] last modified time: (Memory) [{}] vs (File) [{}]", new Object[]{child, lastModifiedTimestamp, fileToWatch.lastModified()});
                    if (lastModifiedTimestamp < fileToWatch.lastModified()) {
                        //update lastModifiedTimestamp
                        lastModifiedTimestamp = fileToWatch.lastModified();
                        setChanged();
                        notifyObservers(modifiedCount.addAndGet(1));

                        log.trace("modifiedCount >> [{}]", modifiedCount.get());
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    continue;
                }
            }
        }
    }

    // Comes straight from the Oracle, must be good
    // see http://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public int getModifiedCount() {
        return this.modifiedCount.get();
    }
}