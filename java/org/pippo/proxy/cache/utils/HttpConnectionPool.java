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

import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import com.sun.net.ssl.internal.ssl.Provider;
import java.io.*;
import java.security.Security;
import java.util.Properties;
import java.util.Vector;

import org.pippo.proxy.cache.utils.log.MyLogger;
import org.pippo.proxy.cache.utils.log.WebLogger;

public class HttpConnectionPool {
	
	private static HttpConnectionPool instance = null;
	
	private String protocol;
	
	private String remoteHost;
	private int remotePort;
	
	private int initConnection, maxConnection;
	
	private boolean proxyEnabled;
	private int proxyPort;
	private String proxyHost;
	
	private Vector freeConnections;

	public static HttpConnectionPool registerInstance(Properties config) throws Exception {
		instance = new HttpConnectionPool(config);
		return instance;
	}

	public static HttpConnectionPool getInstance() {
		return instance;
	}

	private HttpConnectionPool(Properties config) throws Exception {
//		java.security.Security
//				.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		proxyEnabled = false;
		freeConnections = new Vector();
		loadParameters(config);
		inizializePool(initConnection);
		WebLogger.debug("initialized HttpConnectionPool: "+freeConnections.size()+" available conns");
		
	}
	private void loadParameters(Properties config) throws Exception {
		proxyEnabled = 
			(config.getProperty("PROXY_ENABLED") != null 
					&& config.getProperty("PROXY_ENABLED").trim().equalsIgnoreCase("true"));
		if (proxyEnabled) {
			proxyHost = getValueOrThrow(config,"PROXY_HOST");
			proxyPort = Integer.parseInt(config.getProperty("PROXY_PORT") != null
							&& ! "".equals(config.getProperty("PROXY_PORT").trim()) 
							? config.getProperty("PROXY_PORT")
							: "80");
		}

		protocol = config.getProperty("PROTOCOL");
		if ( protocol==null || "".equals(protocol.trim()) ) protocol="http";
		
		remoteHost = getValueOrThrow(config,"REMOTE_HOST"); 
		remotePort = Integer.parseInt(config.getProperty("REMOTE_PORT") != null
						&& !"".equals(config.getProperty("REMOTE_PORT").trim()) 
						? config.getProperty("REMOTE_PORT")
						: "80");
		if (config.getProperty("INIT_CONNECTION") != null) {
			initConnection = Integer.parseInt(config.getProperty("INIT_CONNECTION"));
		} else {
			initConnection = 10;
		}
		if (config.getProperty("MAX_CONNECTION") != null) {
			maxConnection = Integer.parseInt(config.getProperty("MAX_CONNECTION"));
		} else {
			maxConnection = 10;
		}

		//ssl
		if (protocol.equalsIgnoreCase("https") || true) {
			//TODO SSL support
			//java.security.Security.addProvider(new
			// com.sun.net.ssl.internal.ssl.Provider());
			//System.setProperty("javax.net.ssl.keyStore",
			// config.getProperty("SSL_KEYSTORE_FILE") );
			//System.setProperty("javax.net.ssl.keyStorePassword",
			// config.getProperty("SSL_KEYSTORE_PWD") );
		}
		WebLogger.debug("protocol:"+protocol);
		WebLogger.debug("remoteHost:"+remoteHost);
		WebLogger.debug("remotePort:"+remotePort);
	}
	public synchronized HTTPConnection getConnection() {
		HTTPConnection con;
		if (freeConnections.size() > 0) {
			con = (HTTPConnection) freeConnections.firstElement();
			freeConnections.removeElementAt(0);
		} else {
			con = newConnection();
		}
		return con;
	}
	private HTTPConnection newConnection() {
		HTTPConnection con = null;
		try {
			con = new HTTPConnection(protocol, remoteHost, remotePort);
//			if (realm != null)
//				con.addBasicAuthorization(realm, usr, pwd); 
			MyLogger.info("RPT-HTTPClient/0.3-3E");
			if (proxyEnabled)
				con.setCurrentProxy(proxyHost, proxyPort);
			//				if(protocol.equalsIgnoreCase("https"))
			//					con.setSSLSocketFactory(new SSLSocketFactoryImpl());
		} catch (Exception e) {
			MyLogger.info("HttpConnectionPool. Error making new connection - ",e);
		}
		return con;
	}
	public synchronized void releaseConnection(HTTPConnection con) {
		if (freeConnections.size() < maxConnection) {
			freeConnections.add(con);
		} else {
			con = null;
			System.gc();
		}
	}
	private void inizializePool(int max) {
		for (int i = 0; i < max; i++)
			freeConnections.add(newConnection());
	}
	protected String getValueOrThrow(Properties prop, String key) throws Exception {
		String val = prop.getProperty(key);
		if (val == null) {
			throw new Exception ("no entry for key "+key);
		} else {
			return val;
		}
	}
	public static final void main(String args[]) throws Exception {
		//		try {
		//			//http://dbcds.ita.eur.deuba.com/dbresearch/main.html
		//			Security.addProvider(new Provider());
		//			FileInputStream fis = new FileInputStream(
		//					"D:\\java\\var\\cache\\tmp\\test.properties");
		//			Properties p = new Properties();
		//			p.load(fis);
		//			HttpConnectionPool cp = new HttpConnectionPool(p);
		//			HTTPConnection con = cp.getConnection();
		//			HTTPResponse res = con.Get("/dbresearch/main.html");
		//			BufferedReader br = new BufferedReader(new InputStreamReader(res
		//					.getInputStream()));
		//			for (String line = br.readLine(); line != null; line = br
		//					.readLine())
		//				System.out.println(line);
		//
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}

		String file = "/cgi-bin/webank/pubblica/home_pubblica.jsp";

		Security.addProvider(new Provider());
		FileInputStream fis = new FileInputStream(
				"C:\\java\\http_proxy\\newStuff\\_proxy.properties");
		Properties p = new Properties();
		p.load(fis);
		HttpConnectionPool cp = new HttpConnectionPool(p);
		HTTPConnection con = cp.getConnection();

		System.out.println("before get " + file);

		HTTPResponse res = con.Get(file);

		BufferedReader br = new BufferedReader(new InputStreamReader(res
				.getInputStream()));
		for (String line = br.readLine(); line != null; line = br.readLine())
			System.out.println(line);

	}
}