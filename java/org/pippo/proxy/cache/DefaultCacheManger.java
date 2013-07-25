/*
 *  This file is part of the PippoProxy project
 *  Copyright (C)2004 Gino Tesei
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  gtesei@yahoo.com
 *
 *  The PippoProxy's home page is located at:
 *
 *  http://sourceforge.net/projects/pippoproxy 
 *
 */

package org.pippo.proxy.cache;

import org.pippo.proxy.cache.persistent.PersistentHistoryCacheSync;
import org.pippo.proxy.cache.fetch.RemoteHistoryFetcher;
import org.pippo.proxy.cache.utils.Util;
import org.pippo.proxy.cache.utils.log.MyLogger;
import org.pippo.proxy.cache.persistent.PersistentHistory;

import java.util.TimerTask;


class DefaultCacheManger extends CacheManager {
    
    protected DelegateHistoryCacheSyncSupport memory_cache = null;
    protected PersistentHistoryCacheSync disk_cache = null;
    protected FetcherLifeCycle fetcher = null;
    
    protected boolean started = false;
    
    DefaultCacheManger( int time_to_live , int max_memory_size , int max_disk_size , String cache_path_dir) {
        fetcher = new RemoteHistoryFetcher(config_env); 
        disk_cache = new PersistentHistoryCacheSync( time_to_live , max_disk_size , cache_path_dir);
        memory_cache = new DelegateHistoryCacheSyncSupport(  time_to_live , max_memory_size , disk_cache);
    }
    public FetcherLifeCycle getFetcher() {
        return fetcher;
    }
    public void start() throws java.lang.Exception {
        if ( started ) return;
        fetcher.start();
        disk_cache.start();
        memory_cache.start();
        started = true;
    }
    protected void checkStarted() {
        if ( ! started ) throw new RuntimeException( "CacheManager currently stopped" );
    }
    public void reset() {
        checkStarted();
        MyLogger.info( "********* RESET MEMORY CACHE *********" );
        memory_cache.reset();
        MyLogger.info( "********* RESET DISK CACHE *********" );
        disk_cache.reset();
    }
    public void setMemoryMaxCacheSize(int size) {
        checkStarted();
        memory_cache.setMaxCacheSize( size );
    }
    public int getMemoryMaxCacheSize() {
        checkStarted();
        return memory_cache.getMaxCacheSize();
    }
    public void setPersistentMaxCacheSize(int size) {
        checkStarted();
        disk_cache.setMaxCacheSize( size );
    }
    public int getPersistentMaxCacheSize() {
        checkStarted();
        return disk_cache.getMaxCacheSize();
    }
    public void setTimeToLive( int millis )  {
        checkStarted();
        memory_cache.setTimeToLive( millis );
        disk_cache.setTimeToLive( millis );
    }
    public int getTimeToLive() {
        checkStarted();
        return memory_cache.getTimeToLive(); // they are the same ...
    }
    public void expire(ItemID id) {
        checkStarted();
        boolean expired = false;
        expired = memory_cache.expire( id );
        if (! expired ) {
            expired = disk_cache.expire( id );
        }
    }
    public History fetchHistory(ItemID id) {
        checkStarted();
        History ret = memory_cache.fetchHistory( id );
        if ( ret == null) {
            ret = disk_cache.fetchHistory( id );
        } else {
            return ret;
        }
        if ( ret != null ) {
            Util.getCacheTimer().schedule( new Disk2MemoryTask((PersistentHistory)ret) , 0L);
            return ret;
        } else {
            ret = fetcher.fetchHistory( id );
            if ( ret != null ) {
                Util.getCacheTimer().schedule( new Fetch2MemoryTask( ret ) , 0L);
                return ret;
            } else {
                return null;
            }
        }
    }
    public void stop() {
        if ( ! started ) return;
        started = false;
        memory_cache.stop();
        disk_cache.stop();
        fetcher.stop();
    }
    public void dump() {
        checkStarted();
        MyLogger.info( "********* DUMPING MEMORY CACHE *********" );
        ((DelegateHistoryCacheSyncSupport)memory_cache).dump();
        MyLogger.info( "********* DUMPING DISK CACHE *********" );
        ((PersistentHistoryCacheSync)disk_cache).dump();
    }
    
    //Inner classes
    class Disk2MemoryTask extends TimerTask {
        PersistentHistory hs = null;
        
        Disk2MemoryTask(PersistentHistory _hs) {
            hs = _hs;
        }
        public void run() {
            History in_memory_history = new History(hs.getID(),hs.getValue());
            in_memory_history.setHeaders( hs.getHeaderList() );
            memory_cache.addHistory( in_memory_history , hs.getExpirationTime() );
            disk_cache.expire( hs.getID() );
        }
    }
    class Fetch2MemoryTask extends TimerTask {
        History hs = null;
        
        Fetch2MemoryTask(History _hs) {
            hs = _hs;
        }
        public void run() {
            History in_memory_history = new History( hs.getID() , hs.getValue() );
            in_memory_history.setHeaders( hs.getHeaderList() );
            memory_cache.addHistory( in_memory_history );
        }
    }
}