all :
	make compile
	make jar
	make clean

compile:
	javac -encoding UTF-8 -cp "./lib/*" *.java

jar :
	jar cvmf META-INF/SERVER.MF server.jar Server.class AllFileDirSearch.class Const.class Print.class SyncFileInfo.class Utils.class lib
	jar cvmf META-INF/CLIENT.MF client.jar Client.class AllFileDirSearch.class Const.class Print.class SyncFileInfo.class Utils.class lib

server :
	java -jar server.jar

client :
	java -jar client.jar

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

fclean : clean
	rm -rf *.jar

.PHONY:	all, jar, server, client, window_server, window_client, ubuntu_server, ubuntu_client
