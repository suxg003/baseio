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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.protocol.AbstractFuture;

public abstract class AbstractHttp2Frame extends AbstractFuture implements SocketHttp2Frame {

    private Http2FrameHeader header;
    private ByteBuf buf;

    protected AbstractHttp2Frame(Http2FrameHeader header) {
        this.header = header;
    }

    @Override
    public Http2FrameHeader getHeader() {
        return header;
    }

    public ByteBuf getByteBuf() {
        return buf;
    }

    public void setByteBuf(ByteBuf buf) {
        this.buf = buf;
    }

}
