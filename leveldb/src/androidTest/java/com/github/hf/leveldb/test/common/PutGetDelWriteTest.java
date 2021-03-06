package com.github.hf.leveldb.test.common;

/*
 * Stojan Dimitrovski
 *
 * Copyright (c) 2014, Stojan Dimitrovski <sdimitrovski@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OFz SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.github.hf.leveldb.LevelDB;
import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.util.Bytes;
import com.github.hf.leveldb.util.SimpleWriteBatch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by hermann on 8/16/14.
 */
public abstract class PutGetDelWriteTest extends DatabaseTestCase {

    @Test
    public void testPut() throws Exception {
        LevelDB db = obtainLevelDB();

        db.put(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}, true);
        db.put(new byte[]{1, 2, 3, 4}, new byte[]{1, 2, 3, 4}, false);

        db.put(new byte[]{1, 2, 3, 4, 5}, null, false);

        boolean threw = false;

        try {
            db.put((byte[]) null, null, false);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw);

        db.close();

        threw = false;

        try {
            db.put(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}, false);
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertTrue(threw);
    }

    @Test
    public void testGet() throws Exception {
        LevelDB db = obtainLevelDB();

        db.put(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}, false);

        byte[] result = db.get(new byte[]{1, 2, 3});

        assertNotNull(result);
        assertEquals(0, Bytes.lexicographicCompare(new byte[]{1, 2, 3}, result));

        db.put(new byte[]{1, 2, 4}, new byte[]{1, 2, 4}, true);

        result = db.get(new byte[]{1, 2, 4});

        assertNotNull(result);
        assertEquals(0, Bytes.lexicographicCompare(new byte[]{1, 2, 4}, result));

        db.put(new byte[]{1, 2, 4}, null, false);

        result = db.get(new byte[]{1, 2, 4});

        assertNull(result);

        boolean threw = false;

        try {
            db.get((byte[]) null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw);

        db.close();

        threw = false;

        try {
            db.get(new byte[]{1, 2, 3});
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertTrue(threw);
    }

    @Test
    public void testDel() throws Exception {
        LevelDB db = obtainLevelDB();

        db.put(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}, false);

        assertNotNull(db.get(new byte[]{1, 2, 3}));

        db.del(new byte[]{1, 2, 3}, false);

        assertNull(db.get(new byte[]{1, 2, 3}));

        db.put(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}, false);

        assertNotNull(db.get(new byte[]{1, 2, 3}));

        db.del(new byte[]{1, 2, 3}, true);

        assertNull(db.get(new byte[]{1, 2, 3}));

        boolean threw = false;

        try {
            db.del((byte[]) null, false);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw);

        db.close();

        threw = false;

        try {
            db.del(new byte[]{1, 2, 3}, false);
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertTrue(threw);
    }

    @Test
    public void testWrite() throws Exception {
        LevelDB db = obtainLevelDB();

        SimpleWriteBatch swb = new SimpleWriteBatch(db);

        swb.put(new byte[]{1, 2, 3}, new byte[]{1, 2, 3});
        swb.put(new byte[]{1, 2, 3, 4}, new byte[]{1, 2, 3, 4});

        db.write(swb, false);

        assertNotNull(db.get(new byte[]{1, 2, 3}));
        assertNotNull(db.get(new byte[]{1, 2, 3, 4}));

        swb = new SimpleWriteBatch(db);

        swb.put(new byte[]{1, 2, 3, 4}, new byte[]{1, 2, 3});
        swb.del(new byte[]{1, 2, 3});

        db.write(swb, true);

        assertNotNull(db.get(new byte[]{1, 2, 3, 4}));
        assertEquals(0, Bytes.lexicographicCompare(db.get(new byte[]{1, 2, 3, 4}), new byte[]{1, 2, 3}));

        assertNull(db.get(new byte[]{1, 2, 3}));

        boolean threw = false;

        try {
            db.write(null, true);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw);

        db.close();

        threw = false;

        try {
            db.write(new SimpleWriteBatch(), false);
        } catch (LevelDBClosedException e) {
            threw = true;
        }

        assertTrue(threw);
    }
}
