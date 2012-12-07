videochat
=========

Live streaming Red5 based video chat.

# Installation

1) Install Ubuntu Lucid 64bit on machine with not less than 2Gb RAM.

2) Install git client.
```bash
$ sudo apt-get install git-core
```

3) Clone video chat repository: 
```bash
$ mkdir /tmp/bbb
$ cd /tmp/bbb
$ git clone https://github.com/dzer6/videochat.git
```

4) Run xuggler installer under root: 
```bash
$ cd /tmp/bbb/videochat/distr
$ sudo ./xuggle-xuggler.4.0.1049-x86_64-unknown-linux-gnu.sh
```

5) Follow xuggler installer instructions (agree license terms, select directory to install by default)

6) Copy and paste these lines into end of /etc/bash.bashrc:
```bash
export XUGGLE_HOME=/usr/local/xuggler
export LD_LIBRARY_PATH=$XUGGLE_HOME/lib:$LD_LIBRARY_PATH
export PATH=$XUGGLE_HOME/bin:$PATH
```

7) For checking that installation is correct, you should logout, login and invoke: 
```bash
$ ffmpeg
```
You should see: 
```bash
FFmpeg version SVN-r25113-xuggle-4.0.896...
```

8) [Install Oracle JDK6](http://www.webupd8.org/2012/01/install-oracle-java-jdk-7-in-ubuntu-via.html): 
```bash
$ sudo apt-get update && sudo apt-get install python-software-properties
```

if you see:

```bash
perl: warning: Falling back to the standard locale ("C").
perl: warning: Setting locale failed.
perl: warning: Please check that your locale settings:
    LANGUAGE = (unset),
    LC_ALL = (unset),
    LANG = "en_US.UTF-8"
    are supported and installed on your system.
perl: warning: Falling back to the standard locale ("C").
```

read [this](http://askubuntu.com/questions/104169/i-get-this-error-while-updating-and-installing-software-perl-warning-setting)

```bash
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java6-installer
```

9) Copy and paste these lines into end of /etc/bash.bashrc:
```bash
export JAVA_HOME=/usr/lib/jvm/java-6-oracle
```

10) Change line in /etc/bash.bashrc file 
```bash
export PATH=$XUGGLE_HOME/bin:$PATH
```
to
```bash
export PATH=$XUGGLE_HOME/bin:$JAVA_HOME/bin:$PATH
```

11) Install Apache Tomcat: 
```bash
$ cd /tmp/bbb
$ wget http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.33/bin/apache-tomcat-7.0.33.tar.gz
$ tar xvzf apache-tomcat-7.0.33.tar.gz
$ sudo mv apache-tomcat-7.0.33 /usr/local/tomcat
```

12) Configure Tomcat

Add next line 
```bash
JAVA_OPTS="-Djava.awt.headless=true -Dfile.encoding=UTF-8 -server -Xms1536m -Xmx1536m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=256m -XX:MaxPermSize=256m -XX:+DisableExplicitGC"
```
before line
```bash
# OS specific support.  $var _must_ be set to either true or false.
```


13) For automatic tomcat starting 
```bash
$ sudo nano /etc/init.d/tomcat
```

Now paste in the following:

```bash
# Tomcat auto-start
#
# description: Auto-starts tomcat
# processname: tomcat
# pidfile: /var/run/tomcat.pid

export JAVA_HOME=/usr/lib/jvm/java-6-oracle
export XUGGLE_HOME=/usr/local/xuggler
export LD_LIBRARY_PATH=$XUGGLE_HOME/lib:$LD_LIBRARY_PATH
export PATH=$XUGGLE_HOME/bin:$JAVA_HOME/bin:$PATH

case $1 in
start)
        sh /usr/local/tomcat/bin/startup.sh
        ;; 
stop)   
        sh /usr/local/tomcat/bin/shutdown.sh
        ;; 
restart)
        sh /usr/local/tomcat/bin/shutdown.sh
        sh /usr/local/tomcat/bin/startup.sh
        ;; 
esac    
exit 0
```

14) You’ll need to make the script executable by running the chmod command:
```bash
$ sudo chmod 755 /etc/init.d/tomcat
```

15) The last step is actually linking this script to the startup folders with a symbolic link. Execute these two commands and we should be on our way.
```bash
sudo ln -s /etc/init.d/tomcat /etc/rc1.d/K99tomcat
sudo ln -s /etc/init.d/tomcat /etc/rc2.d/S99tomcat
```

16) For checking that tomcat setup is correct reboot your ubuntu machine, login and invoke
```bash
$ ps ax | grep tomcat
```

You should see similar output:
```bash
  935 ?        Sl     0:06 /usr/lib/jvm/java-6-oracle/bin/java -Djava.util.logging.config.file=/usr/local/tomcat/conf/logging.properties -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.endorsed.dirs=/usr/local/tomcat/endorsed -classpath /usr/local/tomcat/bin/bootstrap.jar:/usr/local/tomcat/bin/tomcat-juli.jar -Dcatalina.base=/usr/local/tomcat -Dcatalina.home=/usr/local/tomcat -Djava.io.tmpdir=/usr/local/tomcat/temp org.apache.catalina.startup.Bootstrap start
 1061 pts/0    S+     0:00 grep --color=auto tomcat
```

17) Install Apache ActiveMQ:
```bash
$ mkdir /tmp/bbb
$ cd /tmp/bbb
$ wget http://www.us.apache.org/dist/activemq/apache-activemq/5.7.0/apache-activemq-5.7.0-bin.tar.gz
$ tar xvzf apache-activemq-5.7.0-bin.tar.gz
$ sudo mv apache-activemq-5.7.0 /usr/local/activemq
```

18) For automatic ActiveMQ starting 
```bash
$ sudo nano /etc/init.d/activemq
```

Now paste in the following:

```bash
# ActiveMQ auto-start
#
# description: Auto-starts activemq
# processname: activemq
# pidfile: /var/run/activemq.pid

export JAVA_HOME=/usr/lib/jvm/java-6-oracle

case $1 in
start)
        sh /usr/local/activemq/bin/activemq start
        ;; 
stop)   
        sh /usr/local/activemq/bin/activemq stop
        ;; 
restart)
        sh /usr/local/activemq/bin/activemq start
        sh /usr/local/activemq/bin/activemq stop
        ;; 
esac    
exit 0
```

19) You’ll need to make the script executable by running the chmod command:
```bash
$ sudo chmod 755 /etc/init.d/activemq
```

20) The last step is actually linking this script to the startup folders with a symbolic link. Execute these two commands and we should be on our way.
```bash
sudo ln -s /etc/init.d/activemq /etc/rc1.d/K99activemq
sudo ln -s /etc/init.d/activemq /etc/rc2.d/S99activemq
```

21) For checking that activemq setup is correct reboot your ubuntu machine, login and invoke
```bash
$ ps ax | grep activemq
```

You should see similar output:
```bash
  835 ?        Sl     0:08 /usr/lib/jvm/java-6-oracle/bin/java -Xms1G -Xmx1G -Djava.util.logging.config.file=logging.properties -Dcom.sun.management.jmxremote -Djava.io.tmpdir=/usr/local/activemq/tmp -Dactivemq.classpath=/usr/local/activemq/conf; -Dactivemq.home=/usr/local/activemq -Dactivemq.base=/usr/local/activemq -Dactivemq.conf=/usr/local/activemq/conf -Dactivemq.data=/usr/local/activemq/data -jar /usr/local/activemq/bin/run.jar start
 1063 pts/0    S+     0:00 grep --color=auto activemq
```

22) Download video chat WAR files:
```bash
$ mkdir /tmp/bbb
$ cd /tmp/bbb
$ wget https://bitbucket.org/dzer6/mvn-repo/raw/master/snapshots/com/dzer6/vc/r5wa/1.0.0/r5wa-1.0.0.war
$ wget https://bitbucket.org/dzer6/mvn-repo/raw/master/snapshots/com/dzer6/vc/ga3/1.0.0/ga3-1.0.0.war
```

23) Unzip WAR files
```bash
$ sudo apt-get install unzip
$ unzip r5wa-1.0.0.war -d r5wa
$ unzip ga3-1.0.0.war -d ROOT
```

24) Configure video-chat:

a) Edit file ROOT/WEB-INF/classes/config.properties
change RTMP_SERVER_URL value: for example to "rtmp://192.168.0.105" (your machine external IP or domain name)
change WEB_SITE_URL value: for example to "www.my-chat-domain-name.com" (your domain name)

b) Edit file r5wa/WEB-INF/classes/r5wa-web.properties
change rtmp.server.address value: for example to "rtmp://192.168.0.105" (your machine external IP or domain name)

25) Remove default web applications in Apache Tomcat:
```bash
$ rm -rfv /usr/local/tomcat/webapps/*
```

26) Deploy downloaded web applications:
```bash
$ mv r5wa/ /usr/local/tomcat/webapps/
$ mv ROOT/ /usr/local/tomcat/webapps/
```
27) Open in browser URL that you edited in file ROOT/WEB-INF/classes/config.properties in key WEB_SITE_URL with tomcat port.
For example: "http://192.168.0.105:8080"
You should see video chat web page.