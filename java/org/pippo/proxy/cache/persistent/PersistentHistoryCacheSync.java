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

import org.pippo.proxy.cache.utils.log.MyLogger;

import org.pippo.proxy.cache.*;
import org.pippo.proxy.cache.utils.Util;
import org.pippo.proxy.cache.utils.ChainedException;

import java.io.File;
import java.io.IOException;

import java.util.TimerTask;

public class PersistentHistoryCacheSync extends HistoryCacheSupportSync  {
    
    public static final int MAX_STARTING_RETRY = 3;
    
    protected String cache_path_dir;
    
    // Constructors ------------------------------------------------------------
    public PersistentHistoryCacheSync( String cache_path_dir ) {
        super();
        this.cache_path_dir = cache_path_dir;
    }
    public PersistentHistoryCacheSync(int timeToLive, int maxCacheSize , String cache_path_dir) { // maxCacheSize in MB on Disk
        super(timeToLive,maxCacheSize);
        if ( maxCacheSize < 10) setMaxCacheSize( 10 ); 
        this.cache_path_dir = cache_path_dir;
    }
    
    // LifeCycle ---------------------------------------------------------------
    /**
     * Override this method in order to perform the initial operations on disk 
     * such as removing all files, creating the dir if needed  ... 
     */
    public void start()  throws  Exception {
        super.start();
        try {
            boolean ok = false;
            int curr_retry = 0;
            while ( ! ok && curr_retry <= MAX_STARTING_RETRY ) {
                try {
                    File dir_as_file = new File(cache_path_dir);
                    if ( dir_as_file.exists() ) {
                        if (! dir_as_file.isDirectory()) {
                            if (++curr_retry <= MAX_STARTING_RETRY) {
                                cache_path_dir += ( "_" + System.currentTimeMillis() );
                                continue;
                            } else {
                                throw new Exception( "the specified cache path is not a directory !!!!!!!!" );
                            }
                        } else {
                            cleanDir();
                            ok = true;
                        }
                    } else {
                        if ( ! dir_as_file.mkdirs() ) {
                            if (++curr_retry <= MAX_STARTING_RETRY) {
                                continue;
                            } else {
                                throw new Exception( "unable to create the cache directory  " + dir_as_file.getName() );
                            }
                        } else {
                            ok = true;
                        }
                    }
                } catch (IOException io) {
                    MyLogger.error( 
                    "PersistentHistoryCacheSync: bad starting ["+curr_retry+"/"+MAX_STARTING_RETRY+"]" , io);
                    ++curr_retry;
                }
            }
        } catch (Throwable t) {
            MyLogger.error( 
            "PersistentHistoryCacheSync !!!!!!! unexpected exception !!!!!!!!!" , t);
            throw new ChainedException( "PersistentHistoryCacheSync ---> bad starting " , t);
        }
    }
    protected void cleanDir() throws IOException {
        File dir_as_file = new File(cache_path_dir);
        File[] files = dir_as_file.listFiles();
        for ( int i = files.length ; --i >= 0; ) {
            if ( ! files[i].delete()  ) {
                MyLogger.info( 
                "WARNING: unable to delete file " + files[i].getName() + " in cache directory "
                + dir_as_file.getName() );
            }
        }
    }
    /**
     * Override this method in order to to perform closing operations such as 
     * as removing all files, deleting the dir if needed  ... 
     */ 
    public void reset() {
        super.reset();
        Util.getCacheTimer().schedule( new TimerTask() {
            public void run() {
                try {
                    cleanDir();
                } catch (Throwable t) {
                    MyLogger.error( 
                    "PersistentHistoryCacheSync---> unexpected exception on reset ..." , t);
                }
            }
        }
        , 0L);
    }
    protected boolean bindInternal(LinkedList node , History history , long expiration_time) {
        try {
            PersistentHistory persistentHistory = new PersistentHistory( history.getID() , history.getValue() , expiration_time,cache_path_dir);
            persistentHistory.setHeaders( history.getHeaderList() );
            node.profile = persistentHistory;
            node.expirationTime = expiration_time;
            return true;
        } catch (IOException e) {
            MyLogger.error( "unable to store the history "+history+" on disk " , e  );
            return false;
        }
    }
    protected boolean perform_post_remove_after_expire() {
        return true; 
    }
    /**
     * Remove the related file on local disk 
     */
    protected void postRemove(final LinkedList node) {
        Util.getCacheTimer().schedule( new TimerTask() {
            public void run()  {
                try {
                    String file_name = ((PersistentHistory)node.profile).getFilename();
                    File file = new File( cache_path_dir , file_name);
                    boolean is_deleted = file.delete();
                    if ( ! is_deleted ) is_deleted = file.delete();   // to do with delay
                    if ( ! is_deleted  ) MyLogger.info( 
                    "PersistentHistoryCacheSync.postRemove: unable to remove " + file_name);
                } catch (java.lang.Throwable t) {
                    MyLogger.error( 
                    "PersistentHistoryCacheSync---> bad postRemove of PersistentHistory :" + node.profile , t);
                }
            }
        }
        , 1000L);
    }
} // class PersistentHistoryCacheSync
