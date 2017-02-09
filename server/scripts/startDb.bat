::java -cp h2-1.4.192.jar org.h2.tools.Server -?

java -cp h2-1.4.192.jar org.h2.tools.Server -baseDir "D:/databases/h2" -tcp 
::-ifExists
::-tcpDaemon 
