#crakken
========

Crakken is a web crawler that is designed to take a snapshot of a site (or group of sites) at a given point in time.  It is built on the [Play! Framework](http://www.playframework.com/) using an [Akka](http://akka.io/) actor system to do the crawling.  The results are stored kept in MongoDB using the [Reactive MongoDB](http://reactivemongo.org/) library.  It is currently is very early alpha stages, but its being actively developed on a fairly regular basis.  Currently it will recursively crawl a site to a specified link depth, while scraping the page for all links and external resources.  Any relatively linked external resources will be normalized into absolute links.  Eventually, the intent is to modify the pages at display time to restore as much functionality as practical without a dependency on the original site.  This enable users to actually compare snapshots of the site at different points in time.  The primary use case for this is for regression testing of sites that mostly consist of static content.


##To release the Crakken
========================

While a precompiled release will eventually be available, in the interim, you can build and run it very easily using the latest [Scala SBT](http://www.scala-sbt.org/).

Once you have installed SBT, and have cloned the repository, you will need to point the configuration file to your MongoDB instance.  This can be done by modifying conf/application.conf to point to your MongoDB instance.  Now from the root of the repository that you just cloned, you can simply type '''sbt start''' and then navigate to http://localhost:9000 to release the Crakken!