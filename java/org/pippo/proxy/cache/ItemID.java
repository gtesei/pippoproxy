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
 * Override this class in order to have a different type of ID for items.  
 */

public class ItemID implements java.io.Serializable {
    
    public static final String NULL_ID = "NULL_ID";
    public static final ItemID NULL_ITEM_ID = new ItemID( NULL_ID );
    
    protected String id;
    
    public ItemID(String id) {
        id = (id == null ? NULL_ID : id);
        this.id = id;
    } 
    public int hashCode() { 
        return id.hashCode(); 
    }
    public boolean equals(Object obj) {
        return ( obj instanceof ItemID
                 && id.equals(((ItemID)obj).id) );
    } 
    public String toString() { 
        return id;
    }
} 

