/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.protocol;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioSocketChannel;

public abstract class AbstractFuture implements Future {

    private static final byte TYPE_PING   = 1;
    private static final byte TYPE_PONG   = 2;
    private static final byte TYPE_SILENT = 0;

    //FIXME isX 使用 byte & x ?
    private boolean  flushed;
    private boolean  isSilent;
    private byte     futureType;
    protected byte[] writeBuffer;
    protected int    writeSize;

    protected ByteBuf allocate(NioSocketChannel channel, int capacity) {
        return channel.allocator().allocate(capacity);
    }

    protected ByteBuf allocate(NioSocketChannel channel, int capacity, int maxLimit) {
        return channel.allocator().allocate(capacity, maxLimit);
    }

    @Override
    public final Future flush() {
        flushed = true;
        return this;
    }

    @Override
    public boolean flushed() {
        return flushed;
    }

    @Override
    public byte[] getWriteBuffer() {
        return writeBuffer;
    }

    @Override
    public int getWriteSize() {
        return writeSize;
    }

    @Override
    public boolean isPing() {
        return futureType == TYPE_PING;
    }

    @Override
    public boolean isPong() {
        return futureType == TYPE_PONG;
    }

    @Override
    public boolean isSilent() {
        return isSilent;
    }

    @Override
    public void release(NioEventLoop eventLoop) {
    }

    protected Future reset() {
        this.flushed = false;
        this.futureType = 0;
        this.isSilent = false;
        this.writeSize = 0;
        this.writeBuffer = null;
        return this;
    }

    @Override
    public Future setPing() {
        this.futureType = TYPE_PING;
        this.isSilent = true;
        return this;
    }

    @Override
    public Future setPong() {
        this.futureType = TYPE_PONG;
        this.isSilent = true;
        return this;
    }

    @Override
    public void setSilent() {
        this.futureType = TYPE_SILENT;
        this.isSilent = true;
    }

    @Override
    public void write(byte b) {
        if (writeBuffer == null) {
            writeBuffer = new byte[256];
        }
        int newcount = writeSize + 1;
        if (newcount > writeBuffer.length) {
            writeBuffer = Arrays.copyOf(writeBuffer, writeBuffer.length << 1);
        }
        writeBuffer[writeSize] = b;
        writeSize = newcount;
    }

    @Override
    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int off, int len) {
        if (writeBuffer == null) {
            if ((len - off) != bytes.length) {
                writeBuffer = new byte[len];
                writeSize = len;
                System.arraycopy(bytes, off, writeBuffer, 0, len);
                return;
            }
            writeBuffer = bytes;
            writeSize = len;
            return;
        }
        int newcount = writeSize + len;
        if (newcount > writeBuffer.length) {
            writeBuffer = Arrays.copyOf(writeBuffer, Math.max(writeBuffer.length << 1, newcount));
        }
        System.arraycopy(bytes, off, writeBuffer, writeSize, len);
        writeSize = newcount;
    }

    @Override
    public void write(String text, ChannelContext context) {
        write(text, context.getCharset());
    }

    @Override
    public void write(String text, Charset charset) {
        write(text.getBytes(charset));
    }

    @Override
    public void write(String text, NioSocketChannel channel) {
        write(text, channel.getContext());
    }

}
