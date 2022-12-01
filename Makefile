all :
	javac -encoding UTF-8 -cp "./lib/*" *.java

window_server :
	java -cp "./lib/*;" Server

window_client :
	java -cp "./lib/*;" Client

ubuntu_server :
	java -cp "./lib/*:" Server

ubuntu_client :
	java -cp "./lib/*:" Client

clean :
	rm -rf *.class

.PHONY:	all, server, client, clean
