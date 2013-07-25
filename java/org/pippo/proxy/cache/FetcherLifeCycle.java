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


public interface FetcherLifeCycle {
    
    /**
     * Override this method in order to perform initialization. 
     */
    public void start() throws java.lang.Exception;
    
    /**
     * Return the TransactionHistory associated with the given
     * ItemID in the cache or null if no TransactionHistory is
     * associated with the given ItemID.
     * @param id the ItemID to retrieve a transaction history
     *           for. 
     */
    public History fetchHistory(ItemID id);
    
    /**
     * Return the TransactionHistory associated with the given
     * ItemID in the cache or null if no TransactionHistory is
     * associated with the given ItemID.
     * @param id the ItemID to retrieve a transaction history
     *           for. 
     */
    
    /**
     * Override this method in order to perform the suitable stop procedure. 
     */
    public void stop();
    
}
