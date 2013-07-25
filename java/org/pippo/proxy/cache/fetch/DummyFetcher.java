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

package org.pippo.proxy.cache.fetch;

import org.pippo.proxy.cache.*;

public class DummyFetcher implements  FetcherLifeCycle {
    
    protected static final int HISTORY_SIZE = 100 * 1000;
    protected static byte[] HISTORY = new byte[HISTORY_SIZE];
    static {
        for (int i = HISTORY.length; --i >= 0; ) {
            HISTORY[i] = (byte)i;
        }
    }
    
    public void start() throws java.lang.Exception { }
    
    public History fetchHistory(ItemID id) {
        return new History( id , HISTORY );
    }
    
    public void stop() { }
    
}
