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
package com.generallycloud.test.others;

import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.common.ThreadUtil;

/**
 * @author wangkai
 *
 */
public class TestReentrantLock {

    public static void main(String[] args) {

        ReentrantLock lock = new ReentrantLock();

        ThreadUtil.exec(() -> {

            lock.lock();

            lock.unlock();

        });

        ThreadUtil.exec(() -> {

            lock.lock();

            lock.unlock();

        });

    }

}
