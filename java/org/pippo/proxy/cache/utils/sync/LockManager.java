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

package org.pippo.proxy.cache.utils.sync;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;

public class LockManager {
    
    public static final boolean CONDITION_VARIABLES_ENABLED = false;
    
    private static int[] id_lock = new int[1];
    private static int id_pool = 0;
    
    private static HashMap thread_to_monitor = new HashMap(); 
    private static int[] thread_to_monitor_lock =  new int[1];
    
    private static ThreadLocal lockComparator = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new LockComparator();
        }
    };
    
    public static int newId() {
        int id;
        synchronized ( id_lock ){
            id = id_pool++;
        }
        return id;
    }
    static void notifyLock( Semaphore lock , boolean isAdded) {
        if ( ! CONDITION_VARIABLES_ENABLED ) throw new SynchronizationException( "condition variables are not enabled" );
        synchronized ( thread_to_monitor_lock ) {
            ArrayList locks = (ArrayList)thread_to_monitor.get( Thread.currentThread() );
            if ( locks == null && isAdded)  locks = new ArrayList();
            if ( isAdded )  {
                locks.add( lock );
                thread_to_monitor.put( Thread.currentThread() , locks);
            }
            else if (locks != null) {
                locks.remove( lock );
                if ( locks.size() <= 0 ) thread_to_monitor.remove( Thread.currentThread() );
            } else {
                thread_to_monitor.remove( Thread.currentThread() );
            }
        }
    }
    static Semaphore[] acquiredLocks() {
        if ( ! CONDITION_VARIABLES_ENABLED ) throw new SynchronizationException( "condition variables are not enabled" );
        Semaphore[] ret = null; 
         synchronized ( thread_to_monitor_lock ) { 
            ArrayList locks = (ArrayList)thread_to_monitor.get( Thread.currentThread() );
            if ( locks == null ) ret = new Semaphore[0];
            else ret = (Semaphore[])locks.toArray( new Semaphore[0] );
         }
         return ret;
    }
    public static void testMultipleLock(Semaphore[] locks , long timeout ) {
        Arrays.sort( locks , ((LockComparator)lockComparator.get()) );
        for ( int i = 0; i < locks.length; ++i ) {
            locks[i].testLock(timeout);
        }
    }
    public static void releaseMultipleLock( Semaphore[] locks ) {
        Arrays.sort( locks , ((LockComparator)lockComparator.get()) );
        for ( int i = locks.length; --i >= 0; ) {
            locks[i].releaseLock();
        }
    }
    private static class LockComparator implements Comparator {
        public int compare(Object o1 , Object o2) {
            Semaphore s1 = (Semaphore)o1;
            Semaphore s2 = (Semaphore)o2;
            return (s1.id() - s2.id());
        }
        public boolean equals(Object obj) {
            return (obj instanceof LockComparator);
        }
    } 
}
