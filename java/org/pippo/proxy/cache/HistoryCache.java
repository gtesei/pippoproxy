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

/**
 * Instances of this class are responsible for maintaining a
 * cache of objects for a CacheManager object. 
 */
public interface HistoryCache extends FetcherLifeCycle {
    
    /**
     * Objects are passed to this method for addition to the
     * cache.  However, this method is not required to actually
     * add an object to the cache if that is contrary to its
     * policy for what object should be added.  This method may
     * also remove objects already in the cache in order to
     * make room for new objects.
     * @param history The TransactionHistory that is being
     *        proposed as an addition to the cache.
     */
    public void addHistory(History history);
    
     /**
     * Objects are passed to this method for addition to the
     * cache.  However, this method is not required to actually
     * add an object to the cache if that is contrary to its
     * policy for what object should be added.  This method may
     * also remove objects already in the cache in order to
     * make room for new objects.
     * @param history The TransactionHistory that is being
     *        proposed as an addition to the cache.
     * @param expiration_time the expiration rime of item in millis 
     */
    public void addHistory(History history , long expiration_time);
    
    /**
     * Reset <b>this</b> HistoryCache.
     */
    public void reset();
    
    /**
     * Set the size in MB of <b>this</b> HistoryCache.
     * @param size the size 
     */
    public void setMaxCacheSize(int size);
    
    /**
     * Return the size of <b>this</b> HistoryCache in MB.
     */
    public int getMaxCacheSize();
    
    /**
     * Set the time to live of items for <b>this</b> HistoryCache.
     * @param time to live in millis
     */
    public void setTimeToLive( int millis ); 
    
    /**
     * Return the current time to live of items for <b>this</b> HistoryCache.
     */
    public int getTimeToLive();
    
    public boolean expire(ItemID id);
    
}
