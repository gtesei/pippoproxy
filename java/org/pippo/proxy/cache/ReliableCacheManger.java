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

import org.pippo.proxy.cache.utils.Util;
import org.pippo.proxy.cache.utils.log.MyLogger;

import java.util.TimerTask;

public class ReliableCacheManger extends CacheManager {
    
    protected static final int MAX_FAULTS = 15;
    protected static final long TIME_WINDOW = 5 * 60 * 1000; //5 min
    protected static final long WAITING_TIME_AFTER_CRASH = 3 * 60 * 60 * 1000; //3 h
    
    protected CacheManager[] adaptee = null;
    protected boolean disableCache;
    protected int current_manager;
    
    protected long last_timestamp ;
    protected int current_failures;
    
    protected FetcherLifeCycle fetcher = null;
    
    public ReliableCacheManger(int time_to_live , int max_memory_size , int max_disk_size , String cache_path_dir) {
        adaptee = new CacheManager[] {
            new DefaultCacheManger(time_to_live,max_memory_size,max_disk_size,cache_path_dir)
        };
        fetcher = adaptee[current_manager].getFetcher(); // they are all the same
        disableCache = true;
    }
    
    public FetcherLifeCycle getFetcher() {
        return fetcher;
    }
    public void start() throws java.lang.Exception {
        start(0);
    }
    protected synchronized void start(int i) throws java.lang.Exception {
        adaptee[i].start();
        current_manager = i;
        current_failures = 0;
        last_timestamp = System.currentTimeMillis();
        disableCache = false;
    }
    protected void handle_internal() { 
        Util.getCacheTimer().schedule( new NotifierFailureTask(), 0L);
    }
    protected void schedule_next_restart_after_crash() {
        Util.getCacheTimer().schedule( new TimerTask() {
            public void run() {
                try {
                    start();
                    MyLogger.info(  "********************* ReliableCacheManger: re-enabling cache ...");
                } catch (Throwable t) {
                    MyLogger.error( "********************* ReliableCacheManger: bad restarting ..." , t);
                }
            }
        } , WAITING_TIME_AFTER_CRASH );
        MyLogger.info( "********************* ReliableCacheManger--->too many faults["+current_failures+">"+MAX_FAULTS+"]: disabling cache ...");
    }
    protected synchronized void _handle_internal() {
        if (++current_failures > MAX_FAULTS && ! disableCache) {
            stop();
            if ( current_manager >= (adaptee.length-1) ) {
                schedule_next_restart_after_crash();
            } else {
            	MyLogger.info("********************* ReliableCacheManger---> changing from " + adaptee[current_manager].getClass().getName() + " to " + adaptee[current_manager+1].getClass().getName() );
                boolean ok = false;
                while ( ! ok && current_manager < (adaptee.length-1) ) {
                    try {
                        start(++current_manager);
                        ok = true;
                    } catch (Throwable t) {
                    	MyLogger.info( "ReliableCacheManger--> unexpected exception on RESTART()" , t);
                    }
                }
                if ( ! ok ) schedule_next_restart_after_crash(); 
            }
        }
        
        if (System.currentTimeMillis() - last_timestamp >  TIME_WINDOW ) {
            current_failures = 0;
            last_timestamp = System.currentTimeMillis();
        }
    }
    public void reset() {
        if ( disableCache ) {
        	MyLogger.info( "ReliableCacheManger: unable to execute reset() since the cache is disabled" );
            return;
        }
        try {
            adaptee[current_manager].reset();
        } catch (Throwable t) {
        	MyLogger.error( "ReliableCacheManger--> unexpected exception on reset" , t);
            handle_internal();
        }
    }
    public void setMemoryMaxCacheSize(int size) {
        if ( disableCache ) {
        	MyLogger.info( "ReliableCacheManger: unable to execute setMemoryMaxCacheSize() since the cache is disabled" );
            return;
        }
        try {
            adaptee[current_manager].setMemoryMaxCacheSize(size);
        } catch (Throwable t) {
        	MyLogger.error( "ReliableCacheManger--> unexpected exception on setMemoryMaxCacheSize()" , t);
            handle_internal();
        }
    }
    public int getMemoryMaxCacheSize() {
        if ( disableCache ) {
        	MyLogger.info( 
            "WARNING:::ReliableCacheManger: getMemoryMaxCacheSize() although the cache is disabled" );
        }
        return adaptee[current_manager].getMemoryMaxCacheSize();
    }
    public void setPersistentMaxCacheSize(int size) {
        if ( disableCache ) {
        	MyLogger.info( 
            "ReliableCacheManger: unable to execute setPersistentMaxCacheSize() since the cache is disabled" );
            return;
        }
        try {
            adaptee[current_manager].setPersistentMaxCacheSize(size);
        } catch (Throwable t) {
        	MyLogger.error( "ReliableCacheManger--> unexpected exception on setMemoryMaxCacheSize()" , t);
            handle_internal();
        }
    }
    public int getPersistentMaxCacheSize() {
        if ( disableCache ) {
        	MyLogger.info(
            "WARNING:::ReliableCacheManger: getPersistentMaxCacheSize() although the cache is disabled" );
        }
        return adaptee[current_manager].getPersistentMaxCacheSize();
    }
    public void setTimeToLive( int millis )  {
        if ( disableCache ) {
        	MyLogger.info( "ReliableCacheManger: unable to execute setTimeToLive() since the cache is disabled" );
            return;
        }
        try {
            adaptee[current_manager].setTimeToLive(millis);
        } catch (Throwable t) {
        	MyLogger.error( "ReliableCacheManger--> unexpected exception on setTimeToLive()" , t);
            handle_internal();
        }
    }
    public int getTimeToLive() {
        if ( disableCache ) {
        	MyLogger.info( 
            "WARNING:::ReliableCacheManger: getTimeToLive() although the cache is disabled" );
        }
        return adaptee[current_manager].getTimeToLive();
    }
    public void expire(ItemID id) {
        if ( disableCache ) {
        	MyLogger.info( 
            "WARNING:::ReliableCacheManger: expire() although the cache is disabled" );
        }
        adaptee[current_manager].expire(id);
    }
    public History fetchHistory(ItemID id) {
        try {
            if ( disableCache ) {
                return fetcher.fetchHistory( id );
            } else {
                return adaptee[current_manager].fetchHistory( id );
            }
        } catch (Throwable t) {
        	MyLogger.error("ReliableCacheManger--> unexpected exception on fetchHistory(id["+id+"])" , t);
            handle_internal();
            if ( ! disableCache ) {
                try {
                    return fetcher.fetchHistory( id );
                } catch (Throwable tt) {}
            }
            return null;
        }
    }
    public void dump() {
        if ( disableCache ) {
        	MyLogger.info( "ReliableCacheManger: unable to execute dump() since the cache is disabled" );
            return;
        }
        try {
            adaptee[current_manager].dump();
        } catch (Throwable t) {
        	MyLogger.error( "ReliableCacheManger--> unexpected exception on dump()" , t);
            handle_internal();
        }
    }
    public synchronized void stop() {
        adaptee[current_manager].stop();
        disableCache = true;
        current_manager = 0;
        current_failures = 0;
        last_timestamp = System.currentTimeMillis();
    }
    
    class NotifierFailureTask extends TimerTask {
        public void run() {
            _handle_internal();
        }
    }
}
