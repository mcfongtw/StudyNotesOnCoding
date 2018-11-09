package com.github.mcfongtw.nio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferUnitTest {
    private ByteBuffer buffer;

    @BeforeEach
    private void setUp() {
        buffer = ByteBuffer.allocate(10);
    }

    @AfterEach
    private void tearDown() {
        buffer.clear();
    }

    @Test
    public void testInitializedByteBuffer() {
        //       [_ _ _ _ _ _ _ _ _ _ ]
        //        ^                   ^
        //position                    limit & capacity
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(10, buffer.capacity());
        Assertions.assertEquals(10, buffer.limit());
    }

    @Test
    public void testWriteThenReadDataFromByteBuffer() {
        buffer.put((byte)1);
        Assertions.assertEquals(1, buffer.position());
        Assertions.assertEquals(10, buffer.limit());
        Assertions.assertTrue(buffer.hasRemaining());
        //       [ 1 _ _ _ _ _ _ _ _ _ ]
        //           ^                 ^
        //     position                limit = capacity


        buffer.flip();

        //       [ 1 _ _ _ _ _ _ _ _ _ ]
        //         ^ ^                 ^
        //  position limit            capacity

        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(1, buffer.limit());
        Assertions.assertEquals(10, buffer.capacity());
        Assertions.assertTrue(buffer.hasRemaining());

        Assertions.assertEquals((byte)1, buffer.get());

        Assertions.assertEquals(1, buffer.position());
        Assertions.assertFalse(buffer.hasRemaining());

        //       [ 1 _ _ _ _ _ _ _ _ _ ]
        //           ^                 ^
        //  position = limit            capacity
    }

    @Test
    public void testNoMoreWriteToByteBuffer() {
        ByteBuffer zeroBuffer = ByteBuffer.allocate(0);
        Assertions.assertFalse(zeroBuffer.hasRemaining());

        try {
            zeroBuffer.put((byte) 1);
            Assertions.fail();
        } catch (BufferOverflowException ignored) {

        }
    }

    @Test
    public void testNoMoreReadToByteBuffer() {
        buffer.put((byte)1);

        buffer.flip();

        buffer.get();

        Assertions.assertEquals(1, buffer.position());
        Assertions.assertEquals(1, buffer.limit());
        Assertions.assertFalse(buffer.hasRemaining());

        try {
            buffer.get();
            Assertions.fail();
        } catch (BufferUnderflowException ignored) {

        }
    }

}
