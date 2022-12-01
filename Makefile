all :
	javac -encoding UTF-8 -cp "./lib/*" *.java

server :
	java -cp "./lib/*;" Server

client :
	java -cp "./lib/*;" Client

clean :
	rm -rf *.class