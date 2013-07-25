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

import java.util.Properties;


public abstract class CacheManager implements FetcherLifeCycle  {
    
    protected static CacheManager instance = null;
    protected static Properties config_env = null;
    
    public static CacheManager getInstance() {
        if ( instance == null ) throw new RuntimeException( "!!!!!!!currently is not registred any cache manager!!!!!!!" );
        return instance;
    }
    
    public static CacheManager registerAndStartReliableCacheManger(
     int time_to_live , int max_memory_size , int max_disk_size , String cache_path_dir , Properties conf ) throws Exception {
//        if (instance != null) {
//            instance.stop();
//            instance = null; // gc
//        }
    	if (instance != null) {
    		return instance;
    	}
    	synchronized (CacheManager.class) {
    		if (instance != null) {
        		return instance;
        	}
	        config_env = conf;
	        instance = new ReliableCacheManger( time_to_live , max_memory_size , max_disk_size , cache_path_dir);
	        instance.start();
    	}
        return instance;
    }
    
    public abstract void reset();
    
    public abstract void  setMemoryMaxCacheSize(int size);
    
    public abstract int  getMemoryMaxCacheSize();
    
    public abstract void  setPersistentMaxCacheSize(int size);
    
    public abstract int  getPersistentMaxCacheSize();
    
    public abstract void  setTimeToLive( int millis );
    
    public abstract int  getTimeToLive();
    
    public abstract void  dump();
    
    public abstract FetcherLifeCycle getFetcher();
    
    public abstract void expire(ItemID id);
}
