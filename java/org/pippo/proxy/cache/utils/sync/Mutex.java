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

import org.pippo.proxy.cache.utils.log.MyLogger;

public class Mutex implements Semaphore {

    private Thread owner = null; 
    private String name = null;
    
    private int lock_count = 0;
    
    private final int _id = LockManager.newId();
    
    private boolean set_threadPriority_enabled = true;
    
    public Mutex(String name) {
        this.name = name; 
    }
    public synchronized void testLock(long timeout) {
        boolean isLocked = false;
        try {
            if ( timeout == 0 ) { // don't wait at all
                isLocked = test_lock_without_blocking();
            } else if( timeout < 0 )  { // wait forever
                while ( ! test_lock_without_blocking() ) {
                    this.wait();
                }
                isLocked = true;
            } else { // wait limited by timeout
                if ( ! test_lock_without_blocking() ) {
                    this.wait( timeout );
                    if ( test_lock_without_blocking() ) isLocked = true;
                } else {
                    isLocked = true;
                }
            }
            
            if (isLocked) {
                set_thread_priority( Thread.MAX_PRIORITY ); // you have the monitor so ... run at max speed !!!
                if ( LockManager.CONDITION_VARIABLES_ENABLED ) LockManager.notifyLock(this,true);
            }
            else {
                throw new SynchronizationException( new StringBuffer()
                                                    .append( "Thread[" )
                                                    .append( Thread.currentThread().getName() )
                                                    .append("] ")
                                                    .append( this )
                                                    .append( " ---> unable to acquire the monitor[interrupted==false]" )
                                                    .toString() 
                                                    );
            }
        } catch (InterruptedException e) {
            throw new SynchronizationException( new StringBuffer()
                                                    .append( "Thread[" )
                                                    .append( Thread.currentThread().getName() )
                                                    .append("] ")
                                                    .append( this )
                                                    .append( " ---> unable to acquire the monitor[interrupted==true]" )
                                                    .toString() 
                                                     , e);
        }
    }
//    public synchronized void testLock(long timeout_to_acquire , long timeout_to_release) {
//        boolean isLocked = false;
//        try {
//            if ( timeout_to_acquire == 0 ) { // don't wait at all
//                isLocked = test_lock_without_blocking();
//            } else if( timeout_to_acquire < 0 )  { // wait forever
//                while ( ! test_lock_without_blocking() ) {
//                    this.wait();
//                }
//                isLocked = true;
//            } else { // wait limited by timeout
//                if ( ! test_lock_without_blocking() ) {
//                    this.wait( timeout_to_acquire );
//                    if ( test_lock_without_blocking() ) isLocked = true;
//                } else {
//                    isLocked = true;
//                }
//            }
//            
//            if (isLocked) {
//                set_thread_priority( Thread.MAX_PRIORITY ); // you have the monitor so ... run at max speed !!!
//                if ( LockManager.CONDITION_VARIABLES_ENABLED ) LockManager.notifyLock(this,true);
//                Util.getLockTimer().schedule( , timeout_to_release );
//            }
//            else {
//                throw new SynchronizationException( new StringBuffer()
//                                                    .append( "Thread[" )
//                                                    .append( Thread.currentThread().getName() )
//                                                    .append("] ")
//                                                    .append( this )
//                                                    .append( " ---> unable to acquire the monitor[interrupted==false]" )
//                                                    .toString() 
//                                                    );
//            }
//        } catch (InterruptedException e) {
//            throw new SynchronizationException( new StringBuffer()
//                                                    .append( "Thread[" )
//                                                    .append( Thread.currentThread().getName() )
//                                                    .append("] ")
//                                                    .append( this )
//                                                    .append( " ---> unable to acquire the monitor[interrupted==true]" )
//                                                    .toString() 
//                                                     , e);
//        }
//    }
    protected void set_thread_priority(int p) {
        if ( set_threadPriority_enabled ) {
            try {
                Thread.currentThread().setPriority( p );
            } catch (SecurityException se) {
                MyLogger.error(  "Current security settings prevent Thread.currentThread().setPriority([priority:"+p+"])" , se);
                set_threadPriority_enabled = false;
            } catch (Throwable t) {
                MyLogger.error(  "bad setThreadPriority[priority:"+p+"]" , t);
            }
        }
    }
    protected synchronized boolean test_lock_without_blocking() {
        if ( owner == null ) {
            owner = Thread.currentThread();
            lock_count = 1;
            return true;
        } else if ( owner == Thread.currentThread() ) {
            ++lock_count;
            return true;
        } else return false;
    }
    public int id() {
        return _id;
    }
    public synchronized void releaseLock() {
        if ( owner != Thread.currentThread() ) {
            throw new SynchronizationException( "Current thread is not the owner of monitor" );
        } else if ( --lock_count <= 0 ) {
            owner = null; 
            lock_count = 0;
            set_thread_priority( Thread.NORM_PRIORITY ); // run normal ...
            if ( LockManager.CONDITION_VARIABLES_ENABLED ) LockManager.notifyLock(this,false);
        }
    }
    public String toString() {
        return new StringBuffer()
                   .append( "Mutex [name=")
                   .append( name )
                   .append( "] [id=")
                   .append( _id )
                   .append( "] [owner=")
                   .append( owner == null ? "NONE" : owner.getName() )
                   .append( "]" )
                   .toString();
    }
//    static class ReleaserTask extends TimerTask {
//        protected Thread thread_to_remove;
//        ReleaserTask( Thread _thread_to_remove) {
//            thread_to_remove = _thread_to_remove;
//        }
//        public void run() {
//            if ( owner != thread_to_remove ) {
//                return;
//            } else {
//                
//            }
//            
//            
//            if ( owner == null ) {
//                owner = Thread.currentThread();
//                lock_count = 1;
//                return true;
//            } else if ( owner == Thread.currentThread() ) {
//                ++lock_count;
//                return true;
//            } else return false;
//        }
//    }
}
