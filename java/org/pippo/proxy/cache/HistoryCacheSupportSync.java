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

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.pippo.proxy.cache.utils.Util;

import org.pippo.proxy.cache.utils.log.MyLogger;
import org.pippo.proxy.cache.utils.sync.Mutex;


/**
 * Instances of this class are responsible for maintaining a
 * cache of objects for a CacheManager object.
 *
 * <b>Note:</b> This implementation is synchronized.
 */

public class HistoryCacheSupportSync implements HistoryCache {
    
    // CONSTANTS ---------------------------------------------------------------
    protected static final int DEFAULT_TIME_TO_LIVE = 120*60*1000; // 2h
    protected static final int DEFAULT_CACHE_SIZE = 1; // in MB
    protected static final long WAITING_TIME_TO_ACQUIRE_LOCK = 5000L; // 5 sec
    
    protected int timeToLive;
    
    protected int maxCacheSize; // in bytes
    protected int currentSize; // in bytes 
    
    protected Map cache = null;
    
    protected LinkedList mru = null;
    protected LinkedList lru = null;
    
    protected Mutex mutex_cache = new Mutex( "mutex_cache" );
    
    
    // Constructors ------------------------------------------------------------
    public HistoryCacheSupportSync() {
        this( DEFAULT_TIME_TO_LIVE , DEFAULT_CACHE_SIZE );
    }
    public HistoryCacheSupportSync(int timeToLive, int maxCacheSize) { // maxCacheSize in MB  
        timeToLive = (timeToLive <= 0 ? DEFAULT_TIME_TO_LIVE : timeToLive);
        this.timeToLive = timeToLive;
        maxCacheSize = ( maxCacheSize > 2 ? maxCacheSize : 2);
        this.maxCacheSize = (maxCacheSize * 1000 * 1000);
        this.currentSize = 0;
        cache = new HashMap();
    }
    
    // LifeCycle ---------------------------------------------------------------
    public void start() throws java.lang.Exception { }
    
    public void stop() { 
        try {
            this.reset();
        } catch (Throwable t) {
            MyLogger.info( "bad reset in stop" , t);
        }
    }
    public void dump() {
        try {
            log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
            log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>> dumping cache[not sync] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
            log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
            LinkedList current = mru;
            int i = 0;
            while ( current != null ) {
                log( "** " + (++i) + ")" + current.profile  + (cache.get( current.profile.getID())!=null) + " expires " 
                + Util.dateToString(current.expirationTime) );
                Iterator hI = current.profile.getHeaders();
                while ( hI.hasNext() )  {
                    History.Header h = (History.Header)hI.next();
                    log( "Header: name=" + h.getName() + "  value=" + h.getValue() );
                }
                current = current.next;
            }
            log( "** current size=="+currentSize+" ***** maxCacheSize="+maxCacheSize);
            log( "** timeToLive=="+timeToLive);
            log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        } catch (Throwable t) {
            log( "-----> bad dump" , t);
        }
    }
    public void reset() {
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> reset cache <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        
        mutex_cache.testLock( WAITING_TIME_TO_ACQUIRE_LOCK );
        try {
            cache.clear();
            currentSize = 0;
            mru = null;
            lru = null;
        } finally {
            mutex_cache.releaseLock();
        }
        
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
    }
    
    // Surface Area ------------------------------------------------------------
    public void addHistory(History history) {
        addHistory(history , -1);
    }
    public void addHistory(History history , long expiration_time) {
        expiration_time = (expiration_time < 0 ? System.currentTimeMillis()+timeToLive : expiration_time);
        
        mutex_cache.testLock( WAITING_TIME_TO_ACQUIRE_LOCK );
        try {
            ItemID id = history.getID();
            if (cache.get(id) == null) {
                if ( history.size() > maxCacheSize ) {
                    log( "-------> unable to add the element ["+ history +"] since it's bigger than maxCacheSize!! ["+history.size()+">"+maxCacheSize+"] " );
                } else {
                    boolean ok;
                    if (currentSize == 0) {
                        lru = mru = new LinkedList();
                        ok = bindInternal(mru , history , expiration_time);
                    } else {
                        while ( (currentSize + history.size()) > maxCacheSize) {
                            remove(lru);
                        }
                        LinkedList newLink = new LinkedList();
                        ok = bindInternal(newLink , history , expiration_time);
                        if ( ok ) {
                            newLink.next = mru;
                            mru.previous = newLink;
                            newLink.previous = null;
                            mru = newLink;
                        }
                    }
                    if ( ok ) {
                        cache.put(id, mru);
                        currentSize += history.size();
                    }
                }
            } else {
                fetchHistory(id);
            }
        } finally {
            mutex_cache.releaseLock();
        }
    }
    protected boolean bindInternal(LinkedList node , History history , long expiration_time) {
        node.profile = history;
        node.expirationTime = expiration_time;
        return true;
    }
    public History fetchHistory(ItemID id) {
        LinkedList foundLink = null;
        if ( id == null) return null;
        
        mutex_cache.testLock( WAITING_TIME_TO_ACQUIRE_LOCK );
        try {
            foundLink = (LinkedList)cache.get(id);
            if (foundLink != null) {
                if (foundLink.expirationTime < System.currentTimeMillis()) {
                    remove(foundLink);
                    foundLink = null;
                } else  if (mru != foundLink) {
                    if ( foundLink == lru ) {
                        lru = foundLink.previous;
                        lru.next = null;
                    }
                    if (foundLink.previous != null) {
                        foundLink.previous.next = foundLink.next;
                    }
                    if (foundLink.next != null) {
                        foundLink.next.previous = foundLink.previous;
                    }
                    mru.previous = foundLink;
                    foundLink.previous = null;
                    foundLink.next = mru;
                    mru = foundLink;
                }
            }
        } finally  {
            mutex_cache.releaseLock();
        }
        
        if (foundLink != null) return foundLink.profile;
        else return null;
    }
    public boolean expire(ItemID id) {
        if ( id == null) return false;
        boolean found = false;
        
        mutex_cache.testLock( WAITING_TIME_TO_ACQUIRE_LOCK );
        try {
            LinkedList foundLink = (LinkedList)cache.get(id);
            if (foundLink != null) {
                remove(foundLink , perform_post_remove_after_expire() );
                found = true;
            }
        } finally {
            mutex_cache.releaseLock();
        }
        
        return found;
    }
    protected boolean perform_post_remove_after_expire() {
        return false; // by default
    }
    // Internals ---------------------------------------------------------------
    protected void remove(LinkedList node , boolean with_post_remove) {
        if (node == null) return;
        
        boolean need_post_remove = false;
        mutex_cache.testLock( WAITING_TIME_TO_ACQUIRE_LOCK );
        try {
            if (mru==node) {
                mru = node.next;
            }
            if (lru==node) {
                lru = node.previous;
            }
            if (node.next!=null) {
                node.next.previous = node.previous;
            }
            if (node.previous!=null) {
                node.previous.next = node.next;
            }
            if ( cache.remove(node.profile.getID()) != null ) {
                currentSize -= node.profile.size();
                if ( cache.size() <= 0 ) reset();
                if (with_post_remove) need_post_remove = true;
            }
        } finally {
            mutex_cache.releaseLock();
        }
        
        if ( need_post_remove ) {
            postRemove(node);
        }
    }
    protected void remove(LinkedList node) {
        remove(node , true);
    }
    /**
     * Do nothing by default. Override in order to perform a customized 
     * finalization or to pass the node to another cache wrapping it in a 
     * customized adapter. 
     */
    protected void postRemove(LinkedList node) { }
    public static void log(String msg) {
        log( msg , null);
    }
    public static void log(String msg , Throwable e) {
        log( MyLogger.DEFAULT_LEVEL , msg , null);
    }
    public static void log(int level , String msg , Throwable e) {
        MyLogger.log( level , new StringBuffer()
        .append( "Thread[" )
        .append(Thread.currentThread().getName())
        .append("]::")
        .append(msg)
        .toString()
        , e);
    }
    public int getMaxCacheSize() { // in MB 
        return (this.maxCacheSize / (1000 * 1000) );
    }  
    public void setMaxCacheSize(int size) { // in MB
        if ( size < 0 ) 
            throw new java.lang.IllegalArgumentException( "cache size must be > 0" );
        size = (size > 2 ? size : 2);
        size = (size * 1000 * 1000);
        boolean need_resize = ( size < currentSize ? true : false);
        
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> cache resize <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        log( "** set size max from "+maxCacheSize+" to " + size);
         
        mutex_cache.testLock( WAITING_TIME_TO_ACQUIRE_LOCK );
        try {
            if ( need_resize ) {
                while (currentSize > size)  remove(lru);
            }
            maxCacheSize = size;
        } finally {
            mutex_cache.releaseLock();
        }
        
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
    }
    public int getTimeToLive() {
        return timeToLive;
    }
    public void setTimeToLive(int millis) {
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>> cache reset time to live <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        log( "** set time to live from "+timeToLive+" to " + millis);
        millis = (millis <= 0 ? DEFAULT_TIME_TO_LIVE : millis);
        int delta = (millis - timeToLive);
        if (delta == 0) return;
        log( "** delta: " + delta);
        log( "** set time to live from "+timeToLive+" to " + millis);
        
         mutex_cache.testLock( WAITING_TIME_TO_ACQUIRE_LOCK );
         try {
             LinkedList current = mru;
             while( current != null ) {
                long tmp = current.expirationTime;
                current.expirationTime = (tmp + delta);
                log( "** " + current.profile + " pass from " + Util.dateToString(tmp) + " to " + Util.dateToString(current.expirationTime) );
                current = current.next;
            }
            timeToLive = millis;
        } finally {
            mutex_cache.releaseLock();
        }
        
        log( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
    }
    protected int getCurrentCacheSize() {
        return currentSize;
    }
    // Inner Class -------------------------------------------------------------
    protected class LinkedList {
        public History profile = null;
        public LinkedList previous = null;
        public LinkedList next = null;
        public long expirationTime;
        public String toString() {
            return new StringBuffer()
            .append("LinkedList[expirationTime:")
            .append( Util.dateToString(expirationTime) )
            .append("] [id: " )
            .append( profile.getID() )
            .append( "]" )
            .toString();
        }
    }
} // class TransactionHistoryCacheSync

