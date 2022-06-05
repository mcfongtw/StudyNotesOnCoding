package com.github.mcfongtw.java7;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

@Slf4j
public class TryWithResourceTest {


    private static class FailedResource implements Closeable {

        @Getter
        private volatile boolean isClosed = false;

        @Override
        public void close() throws IOException {
            isClosed = true;
            log.info("closed: {}", isClosed);
            throw new IOException("Intended failure");
        }
    }


    @Test
    public void testCustomFailedCloseableResource() {

        try (
                FailedResource failedResource = new FailedResource();
                ) {
            // implementation
            Assertions.assertFalse(failedResource.isClosed());
        } catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "Intended failure");
        }
    }

    @Test
    public void testCloseStreamSuccessfully() {
        InputStreamReader spyInputStreamReader = null;
        BufferedReader spyBufferedReader = null;

        try(
                InputStream inputStream = this.getClass().getClassLoader()
                        .getResourceAsStream("logback.xml");
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                ) {
            int data = 0;

            spyInputStreamReader = inputStreamReader;
            spyBufferedReader = bufferedReader;

            StringBuilder stringBuilder = new StringBuilder();
            while( (data = bufferedReader.read()) != -1) {
                stringBuilder.append((char) data);
            }

            log.info("Content : {}", stringBuilder.toString());
        } catch (FileNotFoundException e) {
            log.error("Error: ", e);
        } catch (IOException e) {
            log.error("Error: ", e);
        }

        try {
            Assertions.assertFalse(spyBufferedReader.ready());
            Assertions.fail();
        } catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "Stream closed");
        }

        try {
            Assertions.assertFalse(spyInputStreamReader.ready());
            Assertions.fail();
        } catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "Stream closed");
        }
    }

    @Test
    public void testCloseStreamFailure() {
        InputStreamReader spyInputStreamReader = null;
        BufferedReader spyBufferedReader = null;

        try(
                InputStream inputStream = this.getClass().getClassLoader()
                        .getResourceAsStream("logback.xml");
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            spyInputStreamReader = inputStreamReader;
            spyBufferedReader = bufferedReader;

            throw new RuntimeException("intentional stop");
        } catch (FileNotFoundException e) {
            log.error("Error: ", e);
        } catch (IOException e) {
            log.error("Error: ", e);
        } catch (RuntimeException e) {
            Assertions.assertEquals(e.getMessage(), "intentional stop");
        }

        try {
            Assertions.assertFalse(spyBufferedReader.ready());
            Assertions.fail();
        } catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "Stream closed");
        }

        try {
            Assertions.assertFalse(spyInputStreamReader.ready());
            Assertions.fail();
        } catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "Stream closed");
        }
    }

}
