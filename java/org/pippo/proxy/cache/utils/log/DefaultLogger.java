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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

public class DefaultLogger extends MyLogger implements Runnable {

	protected String name = null;
	protected String logDir = null;
	protected File dir = null;

	protected Thread thread;
	protected LinkedList logEvents;

	protected static SimpleDateFormat dateFormatter =
		new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	protected static SimpleDateFormat filedateFormatter =
		new SimpleDateFormat("yyyy-MM-dd-");
	protected PrintWriter writer;
	protected int writerDate = -1;
	protected Calendar calendar = Calendar.getInstance();

	DefaultLogger(String name, String logDir) throws Exception {
		dir = new File(logDir);
		if (!dir.exists()) {
			dir.mkdirs();
		} else {
			if (!dir.isDirectory())
				throw new RuntimeException(
					"What? " + dir.getAbsolutePath() + " isn't a directory!!");
		}

		this.name = (name != null ? name : "uds");

		logEvents = new LinkedList();

		thread = new Thread(this, "DefaultLogger");
		try {
			thread.setDaemon(true);
			thread.setPriority(Thread.MIN_PRIORITY);
		} catch (java.lang.Throwable t) {
			t.printStackTrace();
		}
		thread.start();
		rotateLogs();
	}

	public void logInternal(int _level, String s, Throwable e) {
		addLogEvent(s, e);
	}

	public void run() {
		for (;;) {
			try {
				LogEvent logEvent = null;
				synchronized (logEvents) {
					while (logEvents.isEmpty()) {
						logEvents.wait();
					}
					logEvent = (LogEvent) logEvents.removeFirst();
				}
				_logInternal(logEvent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	protected void addLogEvent(String msg, Throwable ex) {
        synchronized (logEvents) {
            logEvents.addLast(new LogEvent(msg, ex));
            logEvents.notifyAll();
        }
    }
	
	protected void rotateLogs() throws Exception {
        if (writer != null) {
            calendar.setTime(new java.util.Date());
            if (calendar.get(Calendar.DATE) > writerDate) {
                writer.flush();
                writer.close();
                writer = null;
            }
        }
        if (writer == null) {
            String logFilename = this.name+".log";
            //File file = new File(logDir, filedateFormatter.format(calendar.getTime())+logFilename);
            File file = new File(dir, filedateFormatter.format(calendar.getTime())+logFilename);
            writer = new PrintWriter(new FileWriter(file.getAbsolutePath(), true));
            writerDate = calendar.get(Calendar.DATE);
        }
    }

	protected void _logInternal(LogEvent logEvent) {
		try {
			String msg =
			"[" + dateFormatter.format(logEvent.date) + "] " + logEvent.msg;
			rotateLogs();

			writer.print(msg);
			if (logEvent.ex != null) {
				writer.print(" # ");
				logEvent.ex.printStackTrace(writer);
			}
			writer.println();
			writer.flush();
			
			//echo on stdout 
			System.out.println(msg);
			if (logEvent.ex != null) {
				System.out.print(" # ");
				logEvent.ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected static class LogEvent {
        String msg; 
        Throwable ex;
        java.util.Date date;
	    
        LogEvent(String msg, Throwable ex) {
            this.msg = msg;
            this.ex = ex;
            this.date = new java.util.Date();
        }
    }
}
