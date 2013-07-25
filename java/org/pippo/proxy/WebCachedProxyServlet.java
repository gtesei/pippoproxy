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

package org.pippo.proxy;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Properties;

import org.pippo.proxy.cache.utils.log.MyLogger;
import org.pippo.proxy.cache.utils.log.WebLogger;
import org.pippo.proxy.cache.fetch.SmartItemID;
import org.pippo.proxy.cache.ItemID;
import org.pippo.proxy.cache.CacheManager;
import org.pippo.proxy.cache.History;
import org.pippo.proxy.cache.FetcherLifeCycle;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Iterator;


public class WebCachedProxyServlet extends HttpServlet {
    
    protected boolean correctlyStarted = false;
    protected String loginKey = null; 
    protected String remotePrefix = null;
    protected String protocol = null;
    protected int localPrefixLevels = -1;
    protected boolean enableSessionAttrForLogin = false;
    protected boolean cacheEnabled = false;
   
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
        	
        	//--logger
        	WebLogger.initDefaultLogger(config,"pippo");

        	//--session key 
            enableSessionAttrForLogin = Boolean.valueOf(throwIfNull(config,"ENABLE_SESSION_ATTR_KEY_FOR_LOGIN")).booleanValue();
            if (enableSessionAttrForLogin) {
            	loginKey = throwIfNull(config,"SESSION_ATTR_KEY_FOR_LOGIN");
            }
            
            //--cache 
            cacheEnabled = Boolean.valueOf(throwIfNull(config,"CACHE_ENABLED")).booleanValue();
            //virtual fix values
            int time_to_live = 3600000;
            int max_memory_size = 2;
            int max_disk_size = 2;
            String cache_path_dir = "cache";
            if (cacheEnabled) {
            	time_to_live = Integer.parseInt( throwIfNull(config,"CACHE_TIMEOUT") );
                max_memory_size = Integer.parseInt( throwIfNull(config,"CACHE_MAX_MEMORY_SIZE") );
                max_disk_size = Integer.parseInt( throwIfNull(config,"CACHE_MAX_DISK_SIZE") );
                cache_path_dir = throwIfNull(config,"CACHE_PATH_DIR");
            }
            
            //--remote prefix, local prefix, protocol 
            boolean isRoot = false;
            try {
            	isRoot = Boolean.valueOf(config.getInitParameter("IS_ROOT")).booleanValue();
            } catch (Exception e) { }
            protocol = config.getInitParameter("PROTOCOL");
            if (protocol == null || "".equals(protocol.trim()) ) {
            	protocol = "http";
            }
            
            remotePrefix = config.getInitParameter("REMOTE_PREFIX");
            if (remotePrefix == null || "".equals(remotePrefix.trim()) ) {
            	remotePrefix = "";
            }
            if (! remotePrefix.endsWith("/") ) remotePrefix += "/";
            
            String localPrefix = config.getInitParameter("LOCAL_PREFIX");
            if (isRoot) {
            	localPrefixLevels = 0;
            } else if ( localPrefix == null || "".equals(localPrefix.trim()) ) {
            	localPrefixLevels = 1;
            } else {
            	StringTokenizer tk = new StringTokenizer(localPrefix, "/");
                for ( localPrefixLevels = 1; tk.hasMoreTokens(); localPrefixLevels++)
                	tk.nextToken();
            }
            
            //--wrap other params 
            //TODO prop_conf better
            Properties prop_conf = new Properties();
            Enumeration initParNames = config.getInitParameterNames();
            while (initParNames.hasMoreElements()) {
            	String name = (String)initParNames.nextElement();
            	prop_conf.setProperty(name,config.getInitParameter(name));
            }
            
            CacheManager.registerAndStartReliableCacheManger(
                time_to_live , max_memory_size , max_disk_size , cache_path_dir , prop_conf
            );
            
            //--finally
            correctlyStarted = true;
            dump();
            MyLogger.info(this.getClass().getName()+" correctly started ...");
        } catch (Exception e) {
        	MyLogger.error(this.getClass().getName()+"--> bad start !!!!!", e);
            e.printStackTrace();
        }
    }
    protected void dump() {
    	 MyLogger.info("loginKey:"+loginKey);
    	 MyLogger.info("remotePrefix:"+remotePrefix);
    	 MyLogger.info("protocol:"+protocol);
    	 MyLogger.info("localPrefixLevels:"+localPrefixLevels);
    	 MyLogger.info("enableSessionAttrForLogin:"+enableSessionAttrForLogin);
    	 MyLogger.info("cacheEnabled:"+cacheEnabled);
    }
    public ItemID fromURLToItemID(String url , String queryString) {
        StringBuffer id = new StringBuffer(); 
        StringBuffer remoteUrl = new StringBuffer(); 
        remoteUrl.append( remotePrefix );
        
        StringTokenizer tk = new StringTokenizer(url, "/");
        for (int i = 0; i  < localPrefixLevels && tk.hasMoreTokens(); i++) 
        	tk.nextToken();
        
        while ( tk.hasMoreTokens() ) {
            String token = tk.nextToken();
            id.append( token );
            remoteUrl.append( token );
            if ( tk.hasMoreTokens() ) {
                id.append( "_" );
                remoteUrl.append( "/" );
            }
        }
        return new SmartItemID( id.toString() , remoteUrl.toString() , queryString);
    } 
    protected String throwIfNull(ServletConfig config, String key) throws Exception {
    	String value = config.getInitParameter(key);
        if ( value == null || "".equals(value.trim())) 
        		throw new Exception( "missing key:" + key + " from config");
        return value;
    }
    protected boolean checkLogged( HttpServletRequest request ) throws Exception {
        String loginValue = (String)request.getSession().getAttribute( loginKey );
        if ( loginValue == null ) {
            return false;
        } else {
            return true;
        }
    }
    public void destroy() {
        CacheManager.getInstance().stop();
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
    	String queryString = null;
    	String requestURI = null;
    	
        try {
            if ( ! correctlyStarted ) throw new Exception("WebCachedProxyServlet is not correctly started");
            
            if ( enableSessionAttrForLogin && ! checkLogged(request) ) {
            	response.sendError(HttpServletResponse.SC_FORBIDDEN);
            	MyLogger.error("Attempt to enter by UNAUTHORIZED USER" +
                        "\n remote addr:"  + request.getRemoteAddr() +
    		            "\n remote user:"  + request.getRemoteUser() +
    		            "\n remote host:"  + request.getRemoteHost());
            	return;
            }
            
            // if ok
            queryString = request.getQueryString();
            requestURI = request.getRequestURI();
            
            ItemID id = fromURLToItemID(requestURI , queryString);
            History history = null;
            if (cacheEnabled && queryString == null) {
                history = CacheManager.getInstance().fetchHistory( id );
                if (history == null) {
                    CacheManager.getInstance().expire(id);
                    history = CacheManager.getInstance().fetchHistory( id );
                }
            } else {
                FetcherLifeCycle fetcher = CacheManager.getInstance().getFetcher();
                history = fetcher.fetchHistory( id );
            }
            
            if (history == null) {
            	response.sendError(HttpServletResponse.SC_NOT_FOUND);
            	return;
            }
            byte[] requiredResource = history.getValue();
            if (requiredResource == null || requiredResource.length < 1) {
            	response.sendError(HttpServletResponse.SC_NOT_FOUND);
            	return;
            }
            
            Iterator headerIterator = history.getHeaders();
            while ( headerIterator.hasNext() ) {
                History.Header header = (History.Header)headerIterator.next();
                response.setHeader( header.getName() , header.getValue() );
            }
            
            OutputStream out = response.getOutputStream();
            out.write( requiredResource );
            out.close();
            
            MyLogger.info( this.getClass().getName()+": requestURI[" + requestURI 
                    + "] queryString["+queryString+"[null:"+(queryString==null)+"]"+"]" 
                    + " successfully handled.");
        }  catch (Throwable t) {
        	MyLogger.error(this.getClass().getName()+": requestURI[" + requestURI 
                    + "] queryString["+queryString+"[null:"+(queryString==null)+"]"+"]" 
                    + " bad handled",t);
        	throw new ServletException(t);
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
    	processRequest(request, response);
        //response.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED , "NOT_IMPLEMENTED" ); 
    }
    public String getServletInfo() {
        return "Pippo Proxy 1.0";
    }
}
