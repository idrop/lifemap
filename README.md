LifeMap
=======

Lifemap lets you record your life events on a google map. It's a hobby project to try learn Clojure and jQuery.

If you're interested in the Clojure side, start with src/lifemap/server.clj. For jQuery, see public/js/lifemap.js.

Installation
---------------

* Install java, [leiningen](https://github.com/technomancy/leiningen) and [mongoDB](http://www.mongodb.org/)
* Start mongoDB server with bin/mongod
* git clone https://github.com/idrop/lifemap.git
* cd lifemap
* lein deps
* lein repl
* To start the server in the repl:
    <pre>
     user=> user (use 'lifemap.server)
     user=> (start) 
   </pre>
* Browse to http://localhost:8888
* Login with your facebook account and starting adding events to the map

Usage
-------

I usually run the project inside emacs, like:
<pre>
         M-x swank-clojure-project ;; choose project dir
          (use 'lifemap.server)
           (start)
</pre>


TODO
----

* Security!
* Chrome in Ubuntu crash
* Have more than one lifemap per account
* Share a lifemap or a time range in a lifemap
* Make the maps embeddable in other pages

Email me with any queries at phil at skabenga dotkom
