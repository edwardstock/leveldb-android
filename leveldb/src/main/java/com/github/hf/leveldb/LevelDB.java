package com.github.hf.leveldb;

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

import android.content.Context;

import com.github.hf.leveldb.exception.LevelDBClosedException;
import com.github.hf.leveldb.exception.LevelDBException;
import com.github.hf.leveldb.exception.LevelDBSnapshotOwnershipException;
import com.github.hf.leveldb.implementation.NativeLevelDB;
import com.github.hf.leveldb.implementation.mock.MockLevelDB;

import java.io.Closeable;
import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class LevelDB implements Closeable, AutoCloseable {
    public final static String DEFAULT_DBNAME = "default.ldb";
    public final static String NATIVE_LIB_NAME = "leveldb_jni";

    public static void loadNative() {
        System.loadLibrary(NATIVE_LIB_NAME);
    }

    /**
     * Opens a new native (real) LevelDB at path with specified configuration.
     * @param path the path to the database
     * @param configuration configuration for the database, or null
     * @return a new {@link com.github.hf.leveldb.implementation.NativeLevelDB}
     * @throws LevelDBException
     */
    public static LevelDB open(String path, Configuration configuration) throws LevelDBException {
        return new NativeLevelDB(path, configuration);
    }

    /**
     * Opens a new native (real) LevelDB at path with specified configuration.
     * @param context app context to use app files directory to store db
     * @param configuration configuration for the database, or null
     * @return a new {@link com.github.hf.leveldb.implementation.NativeLevelDB}
     * @throws LevelDBException
     */
    public static LevelDB open(Context context, Configuration configuration) throws LevelDBException {
        String path = context.getFilesDir().toString() + File.separator + DEFAULT_DBNAME;
        return new NativeLevelDB(path, configuration);
    }

    /**
     * Convenience for {@link #open(String, com.github.hf.leveldb.LevelDB.Configuration)}
     * @param context app context to use app files directory to store db
     * @return a new {@link com.github.hf.leveldb.implementation.NativeLevelDB} instance
     * @throws LevelDBException
     */
    public static LevelDB open(Context context) throws LevelDBException {
        String path = context.getFilesDir().toString() + File.separator + DEFAULT_DBNAME;
        return open(path, configure());
    }

    /**
     * Convenience for {@link #open(String, com.github.hf.leveldb.LevelDB.Configuration)}
     * @param path the path to the database
     * @return a new {@link com.github.hf.leveldb.implementation.NativeLevelDB} instance
     * @throws LevelDBException
     */
    public static LevelDB open(String path) throws LevelDBException {
        return open(path, configure());
    }

    /**
     * Use this method to obtain a {@link com.github.hf.leveldb.LevelDB.Configuration} object.
     * @return a new {@link com.github.hf.leveldb.LevelDB.Configuration} object
     */
    public static Configuration configure() {
        return new Configuration();
    }

    /**
     * Creates a new {@link com.github.hf.leveldb.implementation.mock.MockLevelDB} useful in
     * testing in non-Android environments such as Robolectric. It does not access the filesystem,
     * and is in-memory only.
     * @return a new {@link com.github.hf.leveldb.implementation.mock.MockLevelDB}
     */
    public static LevelDB mock() {
        return new MockLevelDB();
    }

    /**
     * Destroys the contents of a LevelDB database.
     * @param path the path to the database
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.implementation.NativeLevelDB#destroy(String)
     */
    public static void destroy(String path) throws LevelDBException {
        NativeLevelDB.destroy(path);
    }

    /**
     * If a DB cannot be opened, you may attempt to call this method to resurrect as much of the contents of the
     * database as possible. Some data may be lost, so be careful when calling this function on a database that contains
     * important information.
     * @param path the path to the database
     * @throws com.github.hf.leveldb.exception.LevelDBException
     * @see com.github.hf.leveldb.implementation.NativeLevelDB#repair(String)
     */
    public static void repair(String path) throws LevelDBException {
        checkArgument(path != null, "Path is not specified");
        NativeLevelDB.repair(path);
    }

    /**
     * Closes this LevelDB instance. Database is usually not usable after a call to this method.
     */
    @Override
    public abstract void close();

    /**
     * Writes the key-value pair in the database.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param value non-null, if null same as {@link #del(byte[], boolean)}
     * @param sync whether this write will be forced to disk
     * @throws LevelDBException
     */
    public abstract void put(byte[] key, byte[] value, boolean sync) throws LevelDBException;

    /**
     * Writes the key-value pair in the database.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param value non-null, if null same as {@link #del(byte[], boolean)}
     * @param sync whether this write will be forced to disk
     * @throws LevelDBException
     */
    public void put(@Nonnull String key, String value, boolean sync) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        put(key.getBytes(), value.getBytes(), sync);
    }

    /**
     * Writes the key-value pair in the database.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param value non-null, if null same as {@link #del(byte[], boolean)}
     * @throws LevelDBException
     */
    public void put(@Nonnull String key, String value) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        put(key, value, false);
    }

    /**
     * Asynchronous {@link #put(byte[], byte[], boolean)}.
     * @param key
     * @param value
     * @throws LevelDBException
     */
    public void put(@Nonnull byte[] key, byte[] value) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        put(key, value, false);
    }

    /**
     * Writes a {@link com.github.hf.leveldb.WriteBatch} to the database.
     * @param writeBatch non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param sync whether this write will be forced to disk
     * @throws LevelDBException
     */
    public abstract void write(@Nonnull WriteBatch writeBatch, boolean sync) throws LevelDBException;

    /**
     * Asynchronous {@link #write(WriteBatch, boolean)}.
     * @param writeBatch
     * @throws LevelDBException
     */
    public void write(@Nonnull WriteBatch writeBatch) throws LevelDBException {
        checkArgument(writeBatch != null, "WriteBatch can't be null");
        write(writeBatch, false);
    }

    /**
     * Retrieves key from the database, possibly from a snapshot state.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param snapshot the snapshot from which to read the entry, may be null
     * @return data for the key, or null
     * @throws LevelDBException
     */
    @Nullable
    public abstract byte[] get(@Nonnull byte[] key, Snapshot snapshot) throws LevelDBSnapshotOwnershipException, LevelDBException;

    /**
     * Retrieves key from the database, possibly from a snapshot state.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param snapshot the snapshot from which to read the entry, may be null
     * @return data for the key, or null
     * @throws LevelDBException
     */
    @Nullable
    public byte[] get(@Nonnull String key, Snapshot snapshot) throws LevelDBSnapshotOwnershipException, LevelDBException {
        checkArgument(key != null, "Key can't be null");
        return get(key.getBytes(), snapshot);
    }

    /**
     * Retrieves key from the database, possibly from a snapshot state.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param snapshot the snapshot from which to read the entry, may be null
     * @return String data for the key, or null
     * @throws LevelDBException
     */
    @Nullable
    public String getString(@Nonnull String key, Snapshot snapshot) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        final byte[] data = get(key, snapshot);
        if (data == null) {
            return null;
        }

        return new String(data);
    }

    /**
     * Retrieves key from the database, possibly from a snapshot state.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @return String data for the key, or null
     * @throws LevelDBException
     */
    @Nullable
    public String getString(@Nonnull String key) throws LevelDBException {
        return getString(key, null);
    }

    /**
     * Retrieves key from the database with an implicit snapshot.
     * @see #get(byte[], Snapshot)
     */
    @Nullable
    public byte[] get(@Nonnull byte[] key) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        return get(key, null);
    }

    /**
     * Retrieves key from the database with an implicit snapshot.
     * @see #get(String, Snapshot)
     */
    public byte[] get(@Nonnull String key) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        return get(key, null);
    }

    /**
     * Deletes key from database, if it exists.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param sync whether this write will be forced to disk
     * @throws LevelDBException
     */
    public abstract void del(@Nonnull byte[] key, boolean sync) throws LevelDBException;

    /**
     * Deletes key from database, if it exists.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @throws LevelDBException
     */
    public void del(@Nonnull String key) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        del(key.getBytes(), false);
    }

    /**
     * Deletes key from database, if it exists.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @param sync whether this write will be forced to disk
     * @throws LevelDBException
     */
    public void del(@Nonnull String key, boolean sync) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        del(key.getBytes(), sync);
    }

    /**
     * Asynchronous {@link #del(byte[], boolean)}.
     * @param key
     * @throws LevelDBException
     */
    public void del(@Nonnull byte[] key) throws LevelDBException {
        checkArgument(key != null, "Key can't be null");
        del(key, false);
    }

    /**
     * Raw form of {@link #getProperty(String)}.
     * <p>
     * Retrieves the LevelDB property entry specified with key.
     * @param key non-null, if null throws {@link java.lang.IllegalArgumentException}
     * @return property bytes
     * @throws LevelDBClosedException
     */
    public abstract byte[] getPropertyBytes(byte[] key) throws LevelDBClosedException;

    /**
     * Convenience function.
     * @see com.github.hf.leveldb.LevelDB#getPropertyBytes(byte[])
     */
    public String getProperty(byte[] key) throws LevelDBClosedException {
        byte[] value = getPropertyBytes(key);

        if (value == null) {
            return null;
        }

        return new String(value);
    }

    /**
     * Convenience function.
     * @see com.github.hf.leveldb.LevelDB#getPropertyBytes(byte[])
     */
    public String getProperty(String key) throws LevelDBClosedException {
        return getProperty(key == null ? null : key.getBytes());
    }

    /**
     * Creates a new {@link com.github.hf.leveldb.Iterator} for this database.
     * <p>
     * Data seen by the iterator will be consistent (like a snapshot). Closing the iterator is a must.
     * The database implementation will not close iterators automatically when closed, which may
     * result in memory leaks.
     * @param fillCache whether to fill the internal cache while iterating over the database
     * @param snapshot the snapshot from which to read the entries, may be null
     * @return new iterator
     * @throws LevelDBClosedException
     */
    public abstract Iterator iterator(boolean fillCache, Snapshot snapshot) throws LevelDBSnapshotOwnershipException, LevelDBClosedException;

    /**
     * Iterate over the database with an implicit snapshot created at the time of creation
     * of the iterator.
     * @param fillCache whether to fill the internal cache while iterating over the database
     * @return a new iterator
     * @throws LevelDBClosedException
     */
    public Iterator iterator(boolean fillCache) throws LevelDBClosedException {
        return iterator(fillCache, null);
    }

    /**
     * Iterate over the entries from snapshot while filling the cache.
     * @param snapshot the snapshot from which to read the entries, may be null
     * @return a new iterator
     * @throws LevelDBSnapshotOwnershipException
     * @throws LevelDBClosedException
     */
    public Iterator iterator(Snapshot snapshot) throws LevelDBSnapshotOwnershipException, LevelDBClosedException {
        return iterator(true, snapshot);
    }

    /**
     * Creates a new iterator that fills the cache.
     * @return a new iterator
     * @throws com.github.hf.leveldb.exception.LevelDBClosedException
     * @see #iterator(boolean)
     */
    public Iterator iterator() throws LevelDBClosedException {
        return iterator(true);
    }

    /**
     * The path of this LevelDB. Usually a filesystem path, but may be something else
     * (eg: {@link com.github.hf.leveldb.implementation.mock.MockLevelDB#getPath()}.
     * @return the path of this database, may be null
     */
    public abstract String getPath();

    protected abstract void setPath(String path);

    /**
     * Atomically check if this database has been closed.
     * @return whether it's been closed
     */
    public abstract boolean isClosed();

    /**
     * Obtains a new snapshot of this database's data.
     * <p>
     * Make sure you call {@link #releaseSnapshot(Snapshot)} when you are done with it.
     * @return a new snapshot
     */
    public abstract Snapshot obtainSnapshot() throws LevelDBClosedException;

    /**
     * Releases a previously obtained snapshot. It is not an error to release a snapshot
     * multiple times.
     * <p>
     * If this database does not own the snapshot, a {@link com.github.hf.leveldb.exception.LevelDBSnapshotOwnershipException}
     * will be thrown at runtime.
     * @param snapshot the snapshot to release, if null throws a {@link java.lang.IllegalArgumentException}
     */
    public abstract void releaseSnapshot(Snapshot snapshot) throws LevelDBSnapshotOwnershipException, LevelDBClosedException;

    /**
     * Specifies a configuration to open the database with.
     */
    public static final class Configuration {
        private boolean createIfMissing;
        private int cacheSize;
        private int blockSize;
        private int writeBufferSize;

        private Configuration() {
            createIfMissing = true;
        }

        public boolean createIfMissing() {
            return createIfMissing;
        }

        public Configuration createIfMissing(boolean createIfMissing) {
            this.createIfMissing = createIfMissing;

            return this;
        }

        public int cacheSize() {
            return cacheSize;
        }

        public Configuration cacheSize(int cacheSize) {
            this.cacheSize = Math.abs(cacheSize);

            return this;
        }

        public int blockSize() {
            return this.blockSize;
        }

        public Configuration blockSize(int blockSize) {
            this.blockSize = Math.abs(blockSize);

            return this;
        }

        public int writeBufferSize() {
            return writeBufferSize;
        }

        public Configuration writeBufferSize(int writeBufferSize) {
            this.writeBufferSize = writeBufferSize;

            return this;
        }
    }
}
