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
package com.generallycloud.sample.baseio.http11;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.codec.http11.HttpFuture;
import com.generallycloud.baseio.codec.http11.WebSocketFuture;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.http11.HttpFutureAcceptorService;
import com.generallycloud.baseio.container.http11.HttpSession;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

// FIXME ________根据当前是否正在redeploy来保存和恢复client
@Service("/web-socket-rumpetroll")
public class TestWebSocketRumpetrollServlet extends HttpFutureAcceptorService {

    private Logger              logger     = LoggerFactory.getLogger(getClass());
    private WebSocketMsgAdapter msgAdapter = new WebSocketMsgAdapter();

    @Override
    protected void doAccept(HttpSession session, HttpFuture future) throws Exception {
        future.updateWebSocketProtocol(session.getChannel());
        session.flush(future);
        NioSocketChannel ioChannel = session.getChannel();
        msgAdapter.addClient(ioChannel.getRemoteAddrPort(), ioChannel);
        JSONObject o = new JSONObject();
        o.put("type", "welcome");
        o.put("id", ioChannel.getChannelId());
        WebSocketFuture f = new WebSocketFuture();
        f.write(o.toJSONString(), session.getEncoding());
        session.flush(f);
    }

    @Override
    public void accept(NioSocketChannel channel, Future future) throws Exception {
        if (future instanceof HttpFuture) {
            super.accept(channel, future);
            return;
        }
        WebSocketFuture f = (WebSocketFuture) future;
        // CLOSE
        if (f.isCloseFrame()) {
            if(msgAdapter.removeClient(channel)){
                JSONObject o = new JSONObject();
                o.put("type", "closed");
                o.put("id", channel.getChannelId());
                msgAdapter.sendMsg(o.toJSONString());
                logger.info("客户端主动关闭连接：{}", channel);
            }
            if (channel.isOpened()) {
                channel.flush(f);
            }
        } else {
            String msg = f.getReadText();
            JSONObject o = JSON.parseObject(msg);
            String name = o.getString("name");
            if (StringUtil.isNullOrBlank(name)) {
                name = channel.getRemoteAddrPort();
            }
            o.put("name", name);
            o.put("id", channel.getChannelId());
            String type = o.getString("type");
            if ("update".equals(type)) {
                o.put("life", "1");
                o.put("authorized", "false");
                o.put("x", Double.valueOf(o.getString("x")));
                o.put("y", Double.valueOf(o.getString("x")));
                o.put("momentum", Double.valueOf(o.getString("momentum")));
                o.put("angle", Double.valueOf(o.getString("angle")));
            } else if ("message".equals(type)) {}
            msgAdapter.sendMsg(o.toJSONString());
        }
    }

    @PostConstruct
    public void init() throws Exception {
        msgAdapter.startup("websocket-rumpetroll");
    }

    @PreDestroy
    public void destroy() throws Exception {
        LifeCycleUtil.stop(msgAdapter);
    }

    /**
     * @return the msgAdapter
     */
    public WebSocketMsgAdapter getMsgAdapter() {
        return msgAdapter;
    }

}
