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

import java.io.Serializable;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A couple ID -- value for the cache. Notice that if the cache is persistent 
 * its value should be Serializable or Externalizable. 
 */
public class History implements Serializable {
    
    protected static final byte[] ZERO_BYTE_ARRAY = new byte[0];
    
    protected List headers = null;
    protected ItemID id;
    protected byte[] value = null;
    
    public History() {
        this(null , null);
    }
    public History(ItemID id , byte[] value) {
        id = (id == null ? ItemID.NULL_ITEM_ID : id);
        this.id = id;
        this.value = ( value == null ? ZERO_BYTE_ARRAY : value);
    } 
    public ItemID getID() { 
        return id; 
    }
    public int size() {
        return value.length;
    }
    public String toString() {
        return new StringBuffer()
        .append("History id:")
        .append( id )
        .append( " -- size: " )
        .append( size() )
        .toString();
    }
    public boolean equals(Object obj) {
        if (obj instanceof History) {
            History t = (History)obj;
            return t.getID().equals(id);
        } else {
            return false;
        }
    } 
    public byte[] getValue() {
        return this.value;
    }
    
    public int hashCode() { 
        return id.hashCode();
    }
    
    public void setHeaders(List headers) {
        this.headers = headers;
    }
    public List getHeaderList() {
        return headers;
    }
    public Iterator getHeaders() {
        if ( headers != null ) return headers.iterator();
        else return new ZeroIterator();
    }
    public static class Header {
        String name = null;
        String value = null;
        public Header(String _name , String _value) {
            name = _name;
            value = _value;
        }
        public String getName() {
            return name;
        } 
        public String getValue() {
            return value;
        }
    }
    protected class ZeroIterator implements Iterator {
        public boolean hasNext() {
            return false;
        }
        public Object next() {
            throw new NoSuchElementException();
        }
        public void remove() {}
    }
} 
