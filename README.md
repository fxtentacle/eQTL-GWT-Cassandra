# Introduction

Medical research strives towards a better understanding of the molecular pathophysiology. Due to advancements in laboratory equipment, the number of measurements taken per probe in medical studies could be increased. This increase in observations per individual has to be accompanied by an increase in the number of individuals to account for the problem of multiple testing. The resulting data sets are huge and these technical diffi- culties require medical investigators to seek external advice. Exemplified by problems encountered in the statistical genomics of an immune mediated disease, this work takes a new approach on data presentation to aid in investigative biomedical research.

The key problems addressed are secure and failure-resistant storage of huge data sets and their presentation to the researcher. To aid in the investigative work, the presentation should react to user requests promptly, preferably in real-time. These goals are achieved by capitalizing on related developments in high performance computing and distributed data storage.

# Preparation

You will need:
- one shared PostgreSQL 8.4 server for management data
- one static IP address for every node
- ports 8080 and 9160 mandatory ports 7000 and 7001 optionally accessible peer-to-peer between nodes


# Compiling

First, you will need to create the folder 
	war/WEB-INF/lib 
and place the following jar files into it:
	MD5 (war/WEB-INF/lib/JRI.jar) = 0dafd2203b9af4309b0adf1f8d4c278e
	MD5 (war/WEB-INF/lib/antlr-2.7.6.jar) = 97c6bb68108a3d68094eab0f67157962
	MD5 (war/WEB-INF/lib/commons-collections-3.1.jar) = d1dcb0fbee884bb855bb327b8190af36
	MD5 (war/WEB-INF/lib/dom4j-1.6.1.jar) = 4d8f51d3fe3900efc6e395be48030d6d
	MD5 (war/WEB-INF/lib/gwt-servlet.jar) = 5e873b12c7ece5194196f8da03efedbb
	MD5 (war/WEB-INF/lib/gwt-user.jar) = d846e1c4693d0479f1d9011f830e0cd7
	MD5 (war/WEB-INF/lib/hibernate3.jar) = 84b876f227d80ed857f251800a8bc759
	MD5 (war/WEB-INF/lib/javassist-3.9.0.GA.jar) = e85b97419a1bbea4c6e432b3ab1d4197
	MD5 (war/WEB-INF/lib/jpa-api-2.0-cr-1.jar) = 4a28f3e4fee05e13cf45e6186bf8727e
	MD5 (war/WEB-INF/lib/jta-1.1.jar) = ffc9ca23acc1665c90dbbe715645564f
	MD5 (war/WEB-INF/lib/libthrift-r820831.jar) = d0e0d615bfd80863d050949d0e71404a
	MD5 (war/WEB-INF/lib/log4j-1.2.15.jar) = 4d4609998fbc124ce6f0d1d48fca2614
	MD5 (war/WEB-INF/lib/org.restlet.ext.servlet.jar) = fde50e68350e227aea892f7720077176
	MD5 (war/WEB-INF/lib/org.restlet.jar) = c03b90abd695143f1197c090f1b8d210
	MD5 (war/WEB-INF/lib/persistence-api.jar) = aeb56ad8210370d0cd5c0e995eb0d16c
	MD5 (war/WEB-INF/lib/postgresql-8.4-701.jdbc4.jar) = 298a1db3ea7927d17ce18a5a49c5bbf7
	MD5 (war/WEB-INF/lib/slf4j-api-1.5.8.jar) = f552fed65cab609d57f9aed8de435651
	MD5 (war/WEB-INF/lib/slf4j-log4j12-1.5.8.jar) = e833f7d233d6240c919b57b51a52002e

Next, modify 
	src/hibernate.cfg.xml
and replace the USERNAME and PASSWORD placeholders with the username and password for your PostgreSQL database server.



This git repository will soon contain the source code for my bachelor thesis:
Interactive presentation of expression Quantitative Trait Loci


src/de/uni_luebeck/inb/krabbenhoeft/eQTL/server/processors/ConvertCMorganToBPProcessor_MMus.java

insert centimorgan to basepair mapping tables

INSERT YOUR MARKER DATA HERE

	tmp = new HashMap<Double, Integer>();
	map.put("1", tmp); // chromosome 1
	tmp.put(0.0, 123); // centimorgan 0.0 mappes to basepair 123
	tmp.put(3.0, 456); // centimorgan 3.0 mappes to basepair 456
	tmp.put(10.6, 789); // centimorgan 10.6 mappes to basepair 789


nginx-0.7.65
conf/nginx.conf

upstream  mysite  {
      server   192.168.1.80:8080;
      server   192.168.1.81:8080;
      server   192.168.1.85:8080;
  }


apache-cassandra-0.5.1-src
cassandra on 127.0.0.1", 9160

conf/storage-conf.xml
<Seeds>
    <Seed>192.168.1.81</Seed>
</Seeds>


jetty-6.1.22

webapps/war/

export R_HOME=/Library/Frameworks/R.framework/Resources
export JAVA_OPTIONS=" -ea -d32 -Xms128M -Xmx2G  -Djava.library.path=/Volumes/Important/fxtentacle/Library/R/2.10/library/rJava/jri: "
sh bin/jetty.sh run

contexts/hajo.xml


Required JARs:


License:

gen-java folder:
Apache License 2.0
http://www.apache.org/licenses/LICENSE-2.0

src folder:
GNU Lesser General Public License 3.0
http://www.gnu.org/licenses/lgpl-3.0.html