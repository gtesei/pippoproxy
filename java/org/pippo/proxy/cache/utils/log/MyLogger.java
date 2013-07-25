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

package org.pippo.proxy.cache.utils.log;

public abstract class MyLogger {

	//LOGS LEVELS --------------------------------------------------------------
	public static final int ERROR_LEVEL = 2;
	public static final int INFO_LEVEL = 1;
	public static final int DEBUG_LEVEL = 0;
	public static final int DEFAULT_LEVEL = DEBUG_LEVEL;

	//instance 
	protected static MyLogger instance = null;
	private static boolean enabled = true;
	private static int level = DEBUG_LEVEL;

	public abstract void logInternal(int _level, String s, Throwable e);

	//static 
	public static void log(int _level, String s, Throwable e) {

		if (!enabled || _level < level)
			return;

		if (instance != null) {
			instance.logInternal(_level, s, e);
		} else {
			System.err.println("[No Logger]:" + s);
			if (e != null) {
				e.printStackTrace();
			}
		}
	}
	public static void error(String s, Throwable e) {
		log(ERROR_LEVEL, s, e);
	}
	public static void error(String s) {
		log(ERROR_LEVEL, s, null);
	}

	public static void debug(String s, Throwable e) {
		log(DEBUG_LEVEL, s, e);
	}
	public static void debug(String s) {
		log(DEBUG_LEVEL, s, null);
	}

	public static void info(String s, Throwable e) {
		log(INFO_LEVEL, s, e);
	}
	public static void info(String s) {
		log(INFO_LEVEL, s, null);
	}

	//enabled
	public static void setEnabled(boolean d) {
		enabled = d;
	}
	public static boolean isEnabled() {
		return enabled;
	}

	//level
	public static void setLevel(int l) {
		if (level < 0 || level > ERROR_LEVEL) {
			throw new IllegalArgumentException("bad level:" + l);
		}
		level = l;
	}
	public static int getLevel() {
		return level;
	}
	
	//factory methods
	public static void initDefaultLogger(String name, String logDir)
		throws Exception {
			
		instance = new DefaultLogger(name, logDir);
	}
}
