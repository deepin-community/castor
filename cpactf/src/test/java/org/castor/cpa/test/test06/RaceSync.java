/*
 * Copyright 2009 Udai Gupta, Ralf Joachim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.castor.cpa.test.test06;

import org.junit.Ignore;

/**
 * Test object mapping to test_table used to conduct all the tests.
 */
@Ignore
public final class RaceSync {
    private static final int[] LOCK = new int[0];

    private static int _idcount = 0;

    private int _id;

    private int _value1;

    public RaceSync() {
        synchronized (LOCK) {
            _id = _idcount;
            _idcount++;
        }
        _value1 = 0;
    }

    public void setId(final int id) {
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public synchronized void setValue1(final int value1) {
        _value1 = value1;
    }

    public synchronized int getValue1() {
        return _value1;
    }

    public synchronized void incValue1() {
        _value1++;
    }

    public String toString() {
        return _id + " / " + _value1;
    }
}
