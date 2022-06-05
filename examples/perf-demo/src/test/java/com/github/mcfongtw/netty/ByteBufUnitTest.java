package com.github.mcfongtw.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * https://netty.io/4.1/api/io/netty/buffer/ByteBuf.html
 */
public class ByteBufUnitTest {

    private ByteBuf buffer;

    @BeforeEach
    public void setUp() {
        buffer = Unpooled.buffer(10);
    }

    @AfterEach
    public void tearDown() {
        buffer.release();
    }

    @Test
    public void testInitializedByteBuf() {
        //       [_ _ _ _ _ _ _ _ _ _ ]
        //        ^                   ^
        //readerIndex                capacity
        //writerIndex
        Assertions.assertEquals(buffer.capacity(), 10);
        Assertions.assertEquals(buffer.readerIndex(), 0);
        Assertions.assertEquals(buffer.writerIndex(), 0);
        Assertions.assertEquals(buffer.isDirect(), false);
    }

    @Test
    public void testWriteThenReadDataFromByteBuf() {
        //       [_ _ _ _ _ _ _ _ _ _ ]
        //        ^   ^               ^
        //        |   |              capacity
        //readerIndex |
        //      writerIndex
        buffer.writeByte(1);
        buffer.writeByte(2);
        Assertions.assertEquals(buffer.capacity(), 10);
        Assertions.assertEquals(buffer.readerIndex(), 0);
        Assertions.assertEquals(buffer.writerIndex(), 2);
        Assertions.assertTrue(buffer.isReadable());
        Assertions.assertTrue(buffer.isWritable());

        //       [_ _ _ _ _ _ _ _ _ _ ]
        //          ^ ^               ^
        //          | |              capacity
        // readerIndex|
        //      writerIndex
        buffer.readByte();
        Assertions.assertEquals(buffer.capacity(), 10);
        Assertions.assertEquals(buffer.readerIndex(), 1);
        Assertions.assertEquals(buffer.writerIndex(), 2);
        Assertions.assertTrue(buffer.isReadable());
        Assertions.assertTrue(buffer.isWritable());
    }

    @Test
    public void testNoMoreWriteToByteBuf() {
        buffer = Unpooled.buffer(0);
        Assertions.assertFalse(buffer.isWritable());
        buffer.writeByte(1);
    }

    @Test
    public void testNoMoreReadToByteBuf() {
        buffer.writeByte(1);

        buffer.readByte();
        Assertions.assertEquals(buffer.capacity(), 10);
        Assertions.assertEquals(buffer.readerIndex(), 1);
        Assertions.assertEquals(buffer.writerIndex(), 1);
        Assertions.assertFalse(buffer.isReadable());
        Assertions.assertTrue(buffer.isWritable());

        try {
            buffer.readByte();
            Assertions.fail();
        } catch(IndexOutOfBoundsException ignored) {

        }
    }

    @Test
    public void testDiscardBytesToByteBuf() {
        //       [_ _ _ _ _ _ _ _ _ _ ]
        //          ^ ^               ^
        //          | |              capacity
        // readerIndex|
        //      writerIndex
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.readByte();
        Assertions.assertEquals(buffer.capacity(), 10);
        Assertions.assertEquals(buffer.readerIndex(), 1);
        Assertions.assertEquals(buffer.writerIndex(), 2);
        Assertions.assertTrue(buffer.isReadable());
        Assertions.assertTrue(buffer.isWritable());


        //         [_ _ _ _ _ _ _ _ _ _ ]
        //          ^ ^                 ^
        //          | |                capacity
        // readerIndex|
        //      writerIndex
        buffer.discardReadBytes();
        Assertions.assertEquals(buffer.capacity(), 10);
        Assertions.assertEquals(buffer.readerIndex(), 0);
        Assertions.assertEquals(buffer.writerIndex(), 1);
    }

    @Test
    public void testClearToByteBuf() {
        //       [_ _ _ _ _ _ _ _ _ _ ]
        //          ^ ^               ^
        //          | |              capacity
        // readerIndex|
        //      writerIndex
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.readByte();
        Assertions.assertEquals(buffer.capacity(), 10);
        Assertions.assertEquals(buffer.readerIndex(), 1);
        Assertions.assertEquals(buffer.writerIndex(), 2);
        Assertions.assertTrue(buffer.isReadable());
        Assertions.assertTrue(buffer.isWritable());


        //       [_ _ _ _ _ _ _ _ _ _ ]
        //        ^                   ^
        //readerIndex                capacity
        //writerIndex
        buffer.clear();
        Assertions.assertEquals(buffer.capacity(), 10);
        Assertions.assertEquals(buffer.readerIndex(), 0);
        Assertions.assertEquals(buffer.writerIndex(), 0);
    }
}
