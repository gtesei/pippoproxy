# Pippo Proxy

[![Build Status](https://api.travis-ci.org/gtesei/pippoproxy.svg?branch=master)](https://travis-ci.org/gtesei/pippoproxy)

## What is it?

  PippoProxy is a 100% pure Java HTTP proxy designed/implemented for 
  Apache Tomcat (5.0 or newer). Thanks to its cache manager (that can be 
  disabled in case of DYNAMIC proxied web sites) it's really very performant 
  in case of STATIC proxied web sites (a traditional well known Tomcat limit).
  Untill now, Apache Tomcat provides proxy support by 
  using Apache Web Server proxy support (mod_proxy). But many times, in 
  production applications use a servlet engine (eg. Apache Tomcat) but not 
  Apache Web Server. Moreover, many times java written applications 
  requires java written (sometimes complex) authentication checks (typically 
  checking session attributes). Finally, in case of STATIC proxied web 
  sites  it is very useful a powerful cache in order to minimize remote HTTP 
  GET/POST for boosting performance.  
  This is what PippoProxy provides. 
  
  
## How it works?

  Pippo is a Servlet (and a set of related helper classes). 
  It has a cache manager (that can be disabled in case of DYNAMIC proxied web sites) 
  that manages two caches: one memory based and one file system based. 
  
```
  
  ..................................  ,------------------------------.
  |                                |  |                              |
  | ....... .......      ........  |  | +-----b ........    |------. |
  | |Node1| |Node2| .... |Nodek |  |  | |     | |      |... |Nodeh | |
  | `-----' `-----'      `......|  '''' L_____| |......|    |______| |
  |                                |  |                              |
  |                                |  |                              |
  |                                |  |                              |
   `''''''''''''''''''''''''''''''''  '`'''''''''''''''''''''''''''''
        Memory based Cache                 File System based Cache
          (es. Max 2MB)                       (es. Max 20MB)
  
```

  The caching (Last Recently Used) algorithm considers a (in memory) queue of nodes 
  and for each page hit the related resource (html page, image, ...) is pushed 
  to the head of queue shifting down all other nodes. For instance, this happens 
  if the page related to Node2 is requested. 

```
  ..................................  ,------------------------------.
  |                                |  |                              |
  | ....... .......      ........  |  | +-----b ........    |------. |
  | |Node2| |Node1| .... |Nodek |  |  | |     | |      |... |Nodeh | |
  | `--:--' `--.--'      `......|  '''' L_____| |......|    |______| |
  |    |_______|                   |  |                              |
  |                                |  |                              |
  |                                |  |                              |
   `''''''''''''''''''''''''''''''''  '`'''''''''''''''''''''''''''''
        Memory based Cache                 File System based Cache
  
```

  Otherwise, if a new resource is requested a new head is created shifting 
  down all other nodes. For instance, this happens id a new resource must be cached. 

```
  ..................................  ,------------------------------.
  |                                |  |                              |
  | ....... .......      ........  |  | +-----b ........    |------. |
  ->| New | |Node2| .... |Nodek |  |  | |     | |      |... |Nodeh | |
  | `--:--' `--.--'      `......|  '''' L_____| |......|    |______| |
  |                                |  |                              |
  |                                |  |                              |
  |                                |  |                              |
   `''''''''''''''''''''''''''''''''  '`'''''''''''''''''''''''''''''
        Memory based Cache                 File System based Cache
  
```

  If the total amount of in memory resources (in MB) excedes the value specified by 
  CACHE_MAX_MEMORY_SIZE (es. 2 MB) then a suitable set of last positioned nodes is discarded 
  to filesystem where another cache works. For instance, this happens (taking previous example) 
  if after loading the Node named <New> in memory the node named <Nodek> must be discarded 
  to file system based cache. 

```
  ..................................  ,------------------------------.
  |                                |  |                              |
  | ....... .......      ........  |  | +-----b ........    |------. |
  ->| New | |Node2| .... |      |  |  | |Nodek| |      |... |Nodeh | |
  | `--:--' `--.--'      `......|  '''' L_____| |......|    |______| |
  |                                |  |                              |
  |                                |  |                              |
  |                                |  |                              |
   `''''''''''''''''''''''''''''''''  '`'''''''''''''''''''''''''''''
        Memory based Cache                 File System based Cache
  
```

  File System based Cache works as well. The caching (Last Recently Used) algorithm here 
  considers a (filesystem) queue and for each node discarded from the in memory cache all nodes here are 
  shifted down in the queue. If a resource is requested, the related node is pushed from the filesystem 
  queue to the head of in memory queue. For instance, this happens if NodeK is requested.

```
  
  ' ..................................  ,------------------------------.
  |                                |  |                              |
  | ....... .......      ........  |  | +-----b ........    |------. |
  | |Nodek| |New  | .... |      |  |  | |     | |      |... |Nodeh | |
  | `--^--' `--.--'      `......|  '''' L_____| |......|    |______| |
  |    |                           |  |    |                         |
  |    |                           |  |    |                         |
  |    |                           |  |    |                         |
   `'''|''''''''''''''''''''''''''''  '`'''|'''''''''''''''''''''''''
       |                                   |
       |___________________________________|
  
```

  Finally, if the total amount of resources stored in filesystem excedes the value (in MB) specified 
  by CACHE_MAX_DISK_SIZE, a suitable set of last positioned nodes is destroyed. 
  
  Resources are refreshed according to CACHE_TIMEOUT value (es. 2h). 

## References 
* [PippoProxy homepage] (http://sourceforge.net/projects/pippoproxy/)
* [Tomcat homepage] (http://tomcat.apache.org/)
* [Ant homepage] (http://ant.apache.org/)
* [HTTPClient homepage] (http://www.innovation.ch/java/HTTPClient/)
* [Design PatternsElements of Reusable Object-Oriented Sofware, E. Gamma, R. Helm, R. Johnson, J. Vlissides (Addison-Wesley Professional, 1995; ISBN0201633612)] ()
