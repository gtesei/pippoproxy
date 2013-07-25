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

package org.pippo.proxy.cache.persistent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;

import org.pippo.proxy.cache.*;
import org.pippo.proxy.cache.utils.log.MyLogger;

public class PersistentHistory extends History {
    
    protected int size;
    protected long expiration_time;
    protected String dir;
    
    public PersistentHistory(ItemID id , byte[] value , long expiration_time , String dir) throws java.io.IOException {
        this.dir = dir;
        this.expiration_time = expiration_time; 
        id = (id == null ? ItemID.NULL_ITEM_ID : id);
        this.id = id;
        value = (value == null ? ZERO_BYTE_ARRAY : value ); 
        size = value.length;
        store( value );
    }
    public long getExpirationTime() {
        return expiration_time;
    }
    static String getFilenameFromID( ItemID id ) {
        return id.toString();
    }
    String getFilename() {
        return getFilenameFromID( id );
    }
    protected void store( byte[] value ) throws java.io.IOException {
        BufferedOutputStream  out = new java.io.BufferedOutputStream(
            new FileOutputStream( new File(dir,getFilenameFromID(id))) , size
        );
        out.write( value );
        out.close();
    }
    // null semantic if IOExceptions or not found ... 
    public byte[] getValue() {
        try {
            BufferedInputStream in = new BufferedInputStream(
                new FileInputStream( new File(dir ,getFilenameFromID(id) ) ) , size
            );
            byte[] tmp = new byte[size];
            in.read( tmp );
            in.close();
            return tmp;
        } catch (Throwable t) {
            MyLogger.error( "PersistentHistory "+toString()+"---> bad getValue " , t);
            return null;
        }
    }
    public int size() {
        return size;
    }
} 
