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
package com.generallycloud.baseio.container.http11;

import com.generallycloud.baseio.codec.http11.HttpFuture;
import com.generallycloud.baseio.component.FutureAcceptor;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.protocol.Future;

public abstract class HttpFutureAcceptorService implements FutureAcceptor {

    @Override
    public void accept(NioSocketChannel channel, Future future) throws Exception {
        ApplicationIoEventHandle handle = (ApplicationIoEventHandle) channel.getIoEventHandle();
        HttpFutureAcceptor containerHandle = (HttpFutureAcceptor) handle.getFutureAcceptor();
        HttpSessionManager manager = containerHandle.getHttpSessionManager();
        HttpFuture httpReadFuture = (HttpFuture) future;
        HttpSession httpSession = manager.getHttpSession(containerHandle, channel, httpReadFuture);
        doAccept(httpSession, httpReadFuture);
    }

    protected abstract void doAccept(HttpSession channel, HttpFuture future) throws Exception;

}
