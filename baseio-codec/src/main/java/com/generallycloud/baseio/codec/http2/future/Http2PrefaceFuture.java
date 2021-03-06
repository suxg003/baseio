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
package com.generallycloud.baseio.codec.http2.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.codec.http2.Http2Session;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractFuture;

public class Http2PrefaceFuture extends AbstractFuture {

    private static byte[]  PREFACE_BINARY = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes();

    private static ByteBuf PREFACE_BUF;
    
    private ByteBuf buf;

    static {
        PREFACE_BUF = UnpooledByteBufAllocator.getHeap().wrap(ByteBuffer.wrap(PREFACE_BINARY));
    }

    public Http2PrefaceFuture(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    private void doComplete(NioSocketChannel channel, ByteBuf buf) throws IOException {
        Http2Session session = Http2Session.getHttp2Session(channel);
        session.setPrefaceRead(false);
        if (!isPreface(buf)) {
            throw new IOException("not http2 preface");
        }
        channel.flush(PREFACE_BUF.duplicate());
    }

    private boolean isPreface(ByteBuf buf) {
        if (PREFACE_BINARY.length > buf.remaining()) {
            return false;
        }
        for (int i = 0; i < PREFACE_BINARY.length; i++) {
            if (PREFACE_BINARY[i] != buf.getByte()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean read(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        ByteBuf buf = this.buf;
        buf.read(buffer);
        if (buf.hasRemaining()) {
            return false;
        }
        doComplete(channel, buf.flip());
        return true;
    }

}
