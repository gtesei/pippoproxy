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

package org.pippo.proxy.cache.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Util {
    
    protected static final String DEFAULT_DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    
    protected static Timer cacheTimer = new Timer( true );
    protected static Timer lockTimer = new Timer( true );
    
    protected static ThreadLocal formatters = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new HashMap();
        }
    };
    
    public static DateFormat getDateFormatter(String pattern) {
        HashMap formatterH = (HashMap)formatters.get();
        DateFormat formatter = (DateFormat)formatterH.get( pattern );
        if ( formatter == null ) {
            formatter = new SimpleDateFormat( pattern );
            formatterH.put( pattern , formatter);
        }
        return formatter;
    }
    public static String dateToString( long date ) {
        return dateToString( date , null);
    }
    public static String dateToString( long date, String pattern ) {
        pattern = ( pattern == null ?  DEFAULT_DATE_PATTERN : pattern);
        DateFormat formatter = getDateFormatter(pattern);
        return formatter.format( new Date(date) );
    }
    public static Timer getCacheTimer() {
            return cacheTimer;
    }
    public static Timer getLockTimer() {
            return lockTimer;
    }
}
