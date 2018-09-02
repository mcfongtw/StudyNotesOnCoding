package com.github.mcfongtw.nio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

public class FileChannelUnitTest {

    private ByteBuffer buffer;

    private static final String FILE_IN_PATH = "/tmp/r.data";

    private static final String FILE_OUT_PATH = "/tmp/w.data";

    @BeforeEach
    public void beforeEach() throws IOException {
        Files.createFile(FileSystems.getDefault().getPath(FILE_IN_PATH));
        Files.createFile(FileSystems.getDefault().getPath(FILE_OUT_PATH));
    }

    @AfterEach
    public void afterEach() throws IOException {
        Files.delete(FileSystems.getDefault().getPath(FILE_IN_PATH));
        Files.delete(FileSystems.getDefault().getPath(FILE_OUT_PATH));
    }

    ////////////////////////////

    @Test
    public void testWritableRandomAccessFile() throws IOException {
        try (
            RandomAccessFile fout = new RandomAccessFile(FILE_OUT_PATH, "rw");
            FileChannel outChannel = fout.getChannel();
        ) {
            buffer = ByteBuffer.allocate(10);
            buffer.put((byte)1);
            outChannel.write(buffer);
        }
    }

    @Test
    public void testNonWritableRandomAccessFile() throws IOException {
        try (
                RandomAccessFile fout = new RandomAccessFile(FILE_IN_PATH, "r");
                FileChannel outChannel = fout.getChannel();
        ) {
            buffer = ByteBuffer.allocate(10);
            buffer.put((byte)1);
            outChannel.write(buffer);
            Assertions.fail();
        } catch(NonWritableChannelException ignore) {

        }
    }

    @Test
    public void testWritableFileOutputStream() throws IOException {
        try (
                FileOutputStream fout = new FileOutputStream(FILE_OUT_PATH);
                FileChannel outChannel = fout.getChannel();
        ) {
            buffer = ByteBuffer.allocate(10);
            buffer.put((byte)1);
            outChannel.write(buffer);
        }
    }

    @Test
    public void testWritableFileInputStream() throws IOException {
        try (
                FileInputStream fout = new FileInputStream(FILE_IN_PATH);
                FileChannel outChannel = fout.getChannel();
        ) {
            buffer = ByteBuffer.allocate(10);
            buffer.put((byte)1);
            outChannel.write(buffer);
            Assertions.fail();
        } catch(NonWritableChannelException ignore) {

        }
    }

    ///////////////////////

    @Test
    public void testReadableRandomAccessFile() throws IOException {
        try (
                RandomAccessFile fout = new RandomAccessFile(FILE_IN_PATH, "rw");
                FileChannel outChannel = fout.getChannel();
        ) {
            buffer = ByteBuffer.allocate(10);
            buffer.put((byte)1);
            outChannel.write(buffer);
            buffer.flip();
            outChannel.read(buffer);
        }
    }

    @Test
    public void testReadableWritableStream() throws IOException {
        try (
                FileInputStream fin = new FileInputStream(FILE_IN_PATH);
                FileOutputStream fout = new FileOutputStream(FILE_OUT_PATH);
                FileChannel inChannel = fin.getChannel();
                FileChannel outChannel = fout.getChannel();
        ) {
            buffer = ByteBuffer.allocate(10);
            buffer.put((byte)1);
            outChannel.write(buffer);
            inChannel.read(buffer);
        }
    }

    @Test
    public void testReadableFileOutputStream() throws IOException {
        try (
                FileOutputStream fout = new FileOutputStream(FILE_IN_PATH);
                FileChannel outChannel = fout.getChannel();
        ) {
            buffer = ByteBuffer.allocate(10);
            outChannel.read(buffer);
            Assertions.fail();
        } catch(NonReadableChannelException ignore) {

        }
    }
}
