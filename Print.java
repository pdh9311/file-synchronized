import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

public class Print {

    public static void serverAccept(Socket socket) {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        System.out.println("[server] 연결 수락");
        System.out.println("\tClient IP : " + isa.getHostString());
        System.out.println("\tClient PORT : " + isa.getPort());
        System.out.println("\tClient HOSTNAME : " + isa.getHostName());
        System.out.println("\tClient ADDRESS : " + isa.getAddress());
    }

    public static void clientConnect(Socket socket) {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        System.out.println("[client] 서버에 연결");
        System.out.println("\tServer IP : " + isa.getHostString());
        System.out.println("\tServer PORT : " + isa.getPort());
        System.out.println("\tServer HOSTNAME : " + isa.getHostName());
        System.out.println("\tServer ADDRESS : " + isa.getAddress());
    }

    public static void MapStringSyncFileInfo(Map<String, SyncFileInfo> map) {
        System.out.println("================= size : " + map.size() +" =================");
        for (Map.Entry<String, SyncFileInfo> entry : map.entrySet()) {
            System.out.println("[" + entry.getKey() + " : " + entry.getValue().getLastModifiedDate() + "]");
        }
    }

    public static void stopWithServer(Socket socket) {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        System.out.println("[서버와 연결 종료] " + isa.getAddress() + ":" + isa.getPort());
    }

    public static void stopWithClient(Socket socket) {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        System.out.println("[클라이언트와 연결 종료] : " + isa.getAddress() + ":" + isa.getPort());
    }

}
