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

import java.util.TimerTask;

public class DelegateHistoryCacheSyncSupport extends HistoryCacheSupportSync {
    
    protected HistoryCache delegate = null;
    
    // Constructors ------------------------------------------------------------
    public DelegateHistoryCacheSyncSupport() {
        super();
    }
    public DelegateHistoryCacheSyncSupport(int timeToLive, int maxCacheSize) { // maxCacheSize in MB  
        super(timeToLive,maxCacheSize);
    }
    public DelegateHistoryCacheSyncSupport(int timeToLive, int maxCacheSize, HistoryCache delegate) { // maxCacheSize in MB  
       super(timeToLive,maxCacheSize);
        this.delegate = delegate;
    }
    protected void postRemove(final LinkedList node) {
        if ( delegate != null && node.expirationTime > System.currentTimeMillis() ) {
            Util.getCacheTimer().schedule(
            new TimerTask() {
                public void run() {
                    delegate.addHistory( node.profile , node.expirationTime );
                }
            }
            , 0L);
        } 
    }
}
