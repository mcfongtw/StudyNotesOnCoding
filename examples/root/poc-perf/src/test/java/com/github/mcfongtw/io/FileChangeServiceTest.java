
package com.github.mcfongtw.io;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
class SimpleFileModifiedObserver implements FileModifiedObserver {

    @Override
    public void update(Observable o, Object arg) {
        log.info("Updated: [{}]", arg);
    }

}

@Slf4j
public class FileChangeServiceTest {

    private FileModifiedObserver fileModifiedObserver = new SimpleFileModifiedObserver();

    private FileChangeService fileChangeService;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final File TEST_FILE = new File("/tmp/file.modified.observable.test.conf");

    @BeforeEach
    public void init() throws IOException {
        fileChangeService = new FileChangeService(TEST_FILE, fileModifiedObserver);
        executorService.submit(fileChangeService);
    }

    @AfterEach
    public void tearDown() throws IOException {
        fileChangeService.close();
        executorService.shutdown();
    }


    @Test
    public void testSimpleModification() throws IOException, InterruptedException {
        Assertions.assertEquals(0, fileChangeService.getModifiedCount());

        Assertions.assertEquals(TEST_FILE.getAbsolutePath(), fileChangeService.getFileToWatch().getAbsolutePath());

        rewrite(TEST_FILE, "123\n");

        //TODO: watcher.take() may take up to 1x second to respond. Need to improve
        Thread.sleep(11_000);

        Assertions.assertEquals(1, fileChangeService.getModifiedCount());

    }



    private static void rewrite(File file, String content) throws IOException {
        FileOutputStream fooStream = new FileOutputStream(file, false); // true to append
        // false to overwrite.
        byte[] date = content.getBytes();
        fooStream.write(date);
        fooStream.close();

        log.info("Rewrite [{}] completed", content);
    }
}
