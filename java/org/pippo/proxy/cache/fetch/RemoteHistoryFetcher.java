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
import org.pippo.proxy.cache.utils.HttpConnectionPool;
import org.pippo.proxy.cache.utils.log.MyLogger;

import java.util.Properties;
import java.util.StringTokenizer;
import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import java.util.Enumeration;
import java.util.ArrayList;


public class RemoteHistoryFetcher implements  FetcherLifeCycle {
    
    protected String[] NOT_ALLOWED_HEADER = null;
    
    protected Properties conf = null;
    
    public RemoteHistoryFetcher(final Properties conf) {
        this.conf = conf;
        setNotAllowedHeaders(conf);
    }
    protected void setNotAllowedHeaders(final Properties conf) {
        String n_a_h = conf.getProperty( "NOT_ALLOWED_HEADERS" );
        if (n_a_h == null) {
            NOT_ALLOWED_HEADER = new String[0];
            MyLogger.info( "RemoteHistoryFetcher:: NOT ALLOWED HTTP HEADERS: NONE " );
        } else {
            StringTokenizer header_tk = new StringTokenizer(n_a_h ,"|");
            ArrayList headerList = new ArrayList();
            while ( header_tk.hasMoreTokens() ) {
                String h = header_tk.nextToken();
                headerList.add( h );
            }
            NOT_ALLOWED_HEADER = (String[])headerList.toArray( new String[0] );
            StringBuffer msg = new StringBuffer();
            msg.append( "RemoteHistoryFetcher:: NOT ALLOWED HTTP HEADERS: " );
            for (int i = NOT_ALLOWED_HEADER.length ; --i >=0; ) {
                msg.append( "\n " ).append( NOT_ALLOWED_HEADER[i] );
            }
            MyLogger.info( msg.toString() );
        }
    }
    /**
     * Initialize pools, test connection, download some initials 
     * resources ...
     */
     public void start() throws java.lang.Exception {
         HttpConnectionPool.registerInstance(conf); // initialize the pool
    } 
    /**
     * Fetch a remote resource ...
     */
    public History fetchHistory(ItemID _id) {
        SmartItemID id = (SmartItemID)_id;
        HTTPConnection con = HttpConnectionPool.getInstance().getConnection();
        HTTPResponse res = null;
        History ret = null;
        try {
        	MyLogger.debug(this.getClass().getName()+": fetching "+id.getURL()+"?"+id.getQueryString());
            res = con.Get( id.getURL()  , id.getQueryString() );
            if ( validate(res) ) {
                ret = new History(id , res.getData() );
                //headers 
                Enumeration headers = res.listHeaders();
                ArrayList headerList = null;
                while ( headers.hasMoreElements() ) {
                    if (headerList == null) headerList = new ArrayList();
                    String name = (String)headers.nextElement();
                    if ( isAllowed(name) ) {
                        String value = res.getHeader(name);
                        headerList.add( new History.Header(name,value) );
                    }
                }
                ret.setHeaders( headerList );
            } 
        } catch (Exception e) {
            MyLogger.error( "RemoteHistoryFetcher--> bad Get" , e);
        } finally {
            HttpConnectionPool.getInstance().releaseConnection( con );
        }
        return ret;
    }
    protected boolean isAllowed(String key) {
        boolean is_allowed = true;
        for( int i = NOT_ALLOWED_HEADER.length; --i >= 0; ) {
            if ( NOT_ALLOWED_HEADER[i].equalsIgnoreCase(key) )  {
                is_allowed = false;
                break;
            }
        }
        return is_allowed;
    }
    protected boolean validate( HTTPResponse rsp ) throws Exception {
        if (rsp.getStatusCode() >= 300) {
            MyLogger.info(  "RemoteHistoryFetcher--> bad response: " + rsp.getReasonLine()
            //+ "\n" + "                    --> request url: " + rsp.getEffectiveURL().toString()
            + "\n" + "                    --> status code: " + rsp.getStatusCode()
            );
            return false;
        }
        else
            return true;
    }
    /**
     * Close pools, test connection, ... 
     */
    public void stop() {
     // do nothing at the moment ...   
    }
} 
