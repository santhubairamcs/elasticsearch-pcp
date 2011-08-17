
*This plugin is currently in early but active development.  There is not a lot exported as yet, but more... much much more is coming*


The Why
=======
"Umm... why shouldn't I just use BigDesk?"
------------------------------------------

BigDesk is excellent, but can you do retrospective analysis on what happened last week?  Can you write a rule that sniffs for certain hardware, operating system, JVM, or ElasticSearch metrics to link into, say, Nagios for alarming?  No?

"What about OpenTSDB?"
----------------------

Yeah!  Now that's an awesome product I have to say.  Those guys at StumbleUpon are brilliant.  Alas, OpenTSDB's major issue is that it needs HBase running, which is one of my personal _favourite_ technologies, but isn't a trivial exercise in deployment and maintenance.  It's a very very very large and powerful hammer.  A Formula One engine; with it's corresponding care and feeding required.

If you want to use ElasticSearch, and you want power, but without too much complexity, you need PCP.

"M'Kay, so WTF _IS_ PCP then..?"
--------------------------------

Introduction
============
PCP (Performance Co-Pilot) is a performance monitoring system developed by SGI many moons ago.  It's not well known, but it is ridiculously powerful, and designed for scale.

http://oss.sgi.com/projects/pcp

SGI still use this to monitor their large clusters.  And they are large indeed...

elasticsearch-pcp utilises the open-source [Parfait Java binding](http://code.google.com/p/parfait/) that helps export metrics of the JVM, and application-level metrics into the PCP system.  PCP combines these with Operating System and Hardware metrics to provide a holistic view of performance data.

Prerequisites
=============
First, ensure you have PCP (Performance Co-Pilot) installed locally for you system. There are installs for the vast majority of needs, including Windows! (you really weren't expecting that were you... :) )

ftp://oss.sgi.com/projects/pcp/download/

Download your distribution and install.  You can verify everything is working correctly by just running 'pcp' to show currently installed version info:

    $ pcp
    Performance Co-Pilot configuration on psmiths-macbook-pro.local:

     platform: Darwin psmiths-MacBook-Pro.local 10.8.0 Darwin Kernel Version 10.8.0: Tue Jun 7 16:33:36 PDT 2011; root:xnu-1504.15.3~1/RELEASE_I386 i386
     hardware: 4 cpus, 2 disks, 8189MB RAM
     timezone: EST-10
         pmcd: Version 3.4.0-1, 3 agents
         pmda: pmcd mmv darwin

Installing elasticsearch-pcp
============================
You should be able to install elasticsearch-pcp via the ElasticSearch plugin system, so assuming your shell is located inside the $ELASTICSEARCH_HOME directory then:

    cd bin
    plugin install Aconex/elasticsearch-pcp

Using
=====
Start up elasticsearch, you'll get confirmation that elasticsearch-pcp is running when you see something like this early on in the logs:

    [2011-08-16 22:07:00,087][INFO ][plugins                  ] [Zaladane] loaded [pcp]

Once you have ElasticSearch running, you'll be able to see the metrics appear:

    $ pminfo mmv.elasticsearch

    mmv.elasticsearch.network.retransSegs
    mmv.elasticsearch.network.passiveOpens
    mmv.elasticsearch.network.outSegs
    mmv.elasticsearch.network.outRsts
    mmv.elasticsearch.network.inSegs
    mmv.elasticsearch.network.inErrs
    mmv.elasticsearch.network.estabResets
    mmv.elasticsearch.network.currEstab
    mmv.elasticsearch.network.attemptFails
    mmv.elasticsearch.network.activeOpens
    mmv.elasticsearch.jvm.memory.used
    mmv.elasticsearch.jvm.memory.parnew.time
    mmv.elasticsearch.jvm.memory.parnew.count
    mmv.elasticsearch.jvm.memory.max
    mmv.elasticsearch.jvm.memory.init
    mmv.elasticsearch.jvm.memory.concurrentmarksweep.time
    mmv.elasticsearch.jvm.memory.concurrentmarksweep.count
    mmv.elasticsearch.jvm.memory.committed


The 'mmv' prefix is because the source data coming into PCP is via the MMV (Memory-Mapped Values) architecture.


"Ok, that's just metric names, show me the values.. ?"
------------------------------------------------------

    $ pminfo -f mmv.elasticsearch.network

    mmv.elasticsearch.network.retransSegs
        value 1524

    mmv.elasticsearch.network.passiveOpens
        value 1389

    mmv.elasticsearch.network.outSegs
        value 789121

    mmv.elasticsearch.network.outRsts
        value -1

    mmv.elasticsearch.network.inSegs
        value 756782

    mmv.elasticsearch.network.inErrs
        value 0

    mmv.elasticsearch.network.estabResets
        value 187

    mmv.elasticsearch.network.currEstab
        value 56

    mmv.elasticsearch.network.attemptFails
        value 399

    mmv.elasticsearch.network.activeOpens
        value 12088

You can tell PCP to print the current values this way, and you can specify a node in the metric namespace hierarchy and it will print all nodes below as well.

"Ok, that's just current values, what about plotting them over time?"
---------------------------------------------------------------------

pminfo just displays one-shot values.  You use pmdumptext to output timeseries info:

    $ pmdumptext -X -t5sec  mmv.elasticsearch.jvm
    [ 1] mmv.elasticsearch.jvm.memory.used
    [ 2] mmv.elasticsearch.jvm.memory.parnew.time
    [ 3] mmv.elasticsearch.jvm.memory.parnew.count
    [ 4] mmv.elasticsearch.jvm.memory.max
    [ 5] mmv.elasticsearch.jvm.memory.init
    [ 6] mmv.elasticsearch.jvm.memory.concurrentmarksweep.time
    [ 7] mmv.elasticsearch.jvm.memory.concurrentmarksweep.count
    [ 8] mmv.elasticsearch.jvm.memory.committed

                 Column	     1	     2	     3	     4	     5	     6	     7	     8
    Wed Aug 17 22:28:06	 3.59M	     ?	     ?	 1.07G	 0.27G	     ?	     ?	 0.27G
    Wed Aug 17 22:28:11	 3.59M	 0.00 	 0.00 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:28:16	 3.59M	 0.00 	 0.00 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:28:51	 3.59M	 0.00 	 0.00 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:28:56	 3.59M	 0.00 	 0.00 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:29:01	 3.59M	 0.00 	 0.00 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:29:06	 3.59M	 6.60 	 0.60 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:29:11	 3.59M	17.01 	 1.40 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:29:16	 3.59M	57.00 	 6.00 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:29:21	 3.59M	78.40 	 8.60 	 1.07G	 0.27G	 1.80 	 0.20 	 0.27G
    Wed Aug 17 22:29:26	 3.59M	65.60 	 9.80 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:29:31	 3.59M	72.80 	 9.60 	 1.07G	 0.27G	 1.80 	 0.20 	 0.27G
    Wed Aug 17 22:29:36	 3.59M	73.80 	 9.40 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:29:41	 3.59M	69.40 	 9.80 	 1.07G	 0.27G	 1.40 	 0.20 	 0.27G
    Wed Aug 17 22:29:46	 3.59M	56.40 	 7.20 	 1.07G	 0.27G	 2.40 	 0.20 	 0.27G
    Wed Aug 17 22:29:51	 3.59M	33.19 	 5.60 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:29:56	 3.59M	52.38 	 7.80 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:30:01	 3.59M	60.23 	 9.20 	 1.07G	 0.27G	 2.00 	 0.20 	 0.27G
    Wed Aug 17 22:30:06	 3.59M	 1.00 	 0.20 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G
    Wed Aug 17 22:30:11	 3.59M	 0.00 	 0.00 	 1.07G	 0.27G	 0.00 	 0.00 	 0.27G


This shows the JVM metrics plotted over 5 second (-t5sec), while I input some data into ES.  You can see the different Garbage collectors count & time metrics exposed as rates (counts/second, time/second)

"Well, that's lovely... but it's just text... And I'm a Chart/Graph sort of guy/girl"
-------------------------------------------------------------------------------------

PCP includes a pmchart tool.  You need to grab the PCP GUI installer from the above download links for *Nix, or the PCP Glider package for Windows.  You can launch pmchart and browse the namespace, or you can configure things on the command line (it has a config file format too, read the man page).

Try this:

    $ pmchart -t5 -c CPU -c DISK -c DISKBYTES -h localhost

You will get a pmchart view launched, connected directly to your localhost which is running PCP, plotting the CPU, Disk (IO) and Disk(bytes) views automatically.  You can then use File->New Chart and browse the 'mmv.elasticsearch' namespace to create new charts.  You can save the views for retrieval later.

*TODO embedded image*

"Ok, that's prettier for sure... But BigDesk shows me this?  How is this better?"
---------------------------------------------------------------------------------

PCP comes with many other powerful tools:

* pmlogger - Ability to automatically log source data to a binary file for retrospective analysis.  All the tools can either connect and read from the live data, or process a PCP archive to look over the data retrospectively.  If you want to review something that happened yesterday in fine detail, you can.
* pmie - Create inference rules to trap conditions of interest.  High CPU?  # Searches more than you're expecting?  Perhaps you want to alarm on this, or execute a script.  the PMIE inference engine built into PCP is unbelievable powerful, and can invoke practically any action you like.  We use it to feed into Nagios alarms.
* pmlogsummary - TODO

Roadmap
=======

* Expose Much more JVM metrics out to PCP
* Search metrics: counters & time, broken down by shards being hit and the CPU utilisation accounting - show how you can spot if you're got 'hot' shards/hosts.
* Indexing metrics - number of add/update, delete's per second, on a per-index basis with roll up.  You'll be able to see which indexes are 'hot'

and lots more