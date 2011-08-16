
*This plugin is currently in early but active development.  There is not a lot exported as yet, but more... much much more is coming*


The Why
=======
"I'm  why shouldn't I just use BigDesk?"

BigDesk is excellent, but can you do retrospective analysis on what happened last week?  Can you write a rule that sniffs for certain hardware, operating system, JVM, or ElasticSearch metrics to link into, say, Nagios for alarming?  No?

You need PCP.

Introduction
============
PCP (Performance Co-Pilot) is a performance monitoring system developed by SGI many moons ago.  It's not well known, but it is ridiculously powerful, and designed for scale.

http://oss.sgi.com/projects/pcp

SGI still use this to monitor their large clusters.  And they are large indeed...

elasticsearch-pcp utilises the open-source [Parfait Java binding](http://code.google.com/p/parfait/) that helps export metrics of the JVM, and application-level metrics into the PCP system.  PCP combines these with Operating System and Hardware metrics to provide a holistic view of performance data.

Prerequisites
=============
First, ensure you have PCP (Performance Co-Pilot) installed locally for you system. There are installs for the vast majority of needs, including Windows!

ftp://oss.sgi.com/projects/pcp/download/

You can verify everything is working correctly by just running 'pcp' to show currently installed version info:

    $ pcp
    Performance Co-Pilot configuration on psmiths-macbook-pro.local:

     platform: Darwin psmiths-MacBook-Pro.local 10.8.0 Darwin Kernel Version 10.8.0: Tue Jun 7 16:33:36 PDT 2011; root:xnu-1504.15.3~1/RELEASE_I386 i386
     hardware: 4 cpus, 2 disks, 8189MB RAM
     timezone: EST-10
         pmcd: Version 3.4.0-1, 3 agents
         pmda: pmcd mmv darwin

Installing
==========
You should be able to install this via the ElasticSearch plugin system, so assuming your shell is located inside the $ELASTICSEARCH_HOME directory then:

    cd bin
    plugin install Aconex/elasticsearch-pcp

Using
=====
Start up elasticsearch, you'll get confirmation that elasticsearch-pcp is running when you see something like this early on in the logs:

    [2011-08-16 22:07:00,087][INFO ][plugins                  ] [Zaladane] loaded [pcp]

Once you have ElasticSearch running, you'll be able to see the metrics appear:

    $ pminfo mmv.elasticsearch

    mmv.elasticsearch.jvm.memory.parnew.time
    mmv.elasticsearch.jvm.memory.parnew.count
    mmv.elasticsearch.jvm.memory.concurrentmarksweep.time
    mmv.elasticsearch.jvm.memory.concurrentmarksweep.count


Roadmap
=======

* Much more JVM metrics
* Search metrics: counters & time
* indexing metrics - number of add/update, delete's per second, on a per-index basis with roll up.  You'll be able to see which indexes are 'hot'

and lots more