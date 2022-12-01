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
	rm -rf *.class *.jar

jar :
	jar cvmf META-INF/SERVER.MF server.jar Server.class AllFileDirSearch.class Const.class Print.class SyncFileInfo.class Utils.class lib
	jar cvmf META-INF/CLIENT.MF client.jar Client.class AllFileDirSearch.class Const.class Print.class SyncFileInfo.class Utils.class lib

jar_server :
	java -jar server.jar

jar_client :
	java -jar client.jar


.PHONY:	all, server, client, clean
