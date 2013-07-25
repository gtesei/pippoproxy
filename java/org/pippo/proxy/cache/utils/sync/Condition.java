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


public class Condition {
    
    private boolean _is_true;
    
    public Condition( boolean is_true ) {
        _is_true = is_true;
    }
    
    public synchronized boolean isTrue() {
        return _is_true;
    }
    public synchronized void setFalse() {
        _is_true = false;
    }
    public synchronized void setTrue() {
        _is_true = true;
    }
    public synchronized void releaseAll() {
        this.notifyAll();
    }
    public synchronized void releaseOne() {
        this.notify();
    }
    public synchronized boolean waitForTrue( long timeout_wait , long timeout_test_lock ) 
    throws InterruptedException
    {
        if ( ! _is_true ) 
            if ( timeout_wait < 0) {
                Semaphore[] acquiredLocks = LockManager.acquiredLocks();
                LockManager.releaseMultipleLock( acquiredLocks );
                this.wait();
                LockManager.testMultipleLock( acquiredLocks , timeout_test_lock);
            }
            else if ( timeout_wait == 0 ) ; // no waiting ...
            else {
                Semaphore[] acquiredLocks = LockManager.acquiredLocks();
                LockManager.releaseMultipleLock( acquiredLocks );
                this.wait( timeout_wait );
                LockManager.testMultipleLock( acquiredLocks , timeout_test_lock);
            }
        return _is_true;
    }
}
