import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static Map<String, SyncFileInfo> prevMap = new HashMap<>();

    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(Const.SERVER_PORT);
        } catch (IOException e) {
            System.out.println("[" + Const.SERVER_PORT + " 포트에 연결할 수 없습니다.]");
        }
        System.out.println("[server] 시작");
        try {
            while (true) {
                // 클라이언트 연결 요청 기다림.
                Socket socket = serverSocket.accept();

                Print.serverAccept(socket);

                // 특정 디렉토리 : ~/backup
                AllFileDirSearch search = new AllFileDirSearch(Const.SERVER_SYNC_DIR);
                prevMap = search.getClientSyncFileInfos();
                Print.MapStringSyncFileInfo(prevMap);

                // 처음 서버에 backup 되어있는 파일들의 정보를 클라이언트한테 보낸다.
                Utils.sendMapStringSyncFileInfo(socket, prevMap);

                // 클라이언트의 현재 sync 되어있는 파일들의 정보를 가져온다.
                Map<String, SyncFileInfo> clientMap = Utils.recvMapStringSyncFileInfo(socket);

                // 클라이언트로 부터 추가, 변경, 삭제에 대한 요청 감지
                Thread thread = eventHandler(socket);

                serverFileCheck(socket, clientMap);
                Utils.sendMsg(socket, "END");
                thread.join();

            }
        } catch (IOException | InterruptedException e) {
            System.out.println("[예외 발생] " + e.getMessage());
        }


    }

    private static void serverFileCheck(Socket socket, Map<String, SyncFileInfo> clientMap) throws IOException {
        for (Map.Entry<String, SyncFileInfo> entry : prevMap.entrySet()) {
            String key = entry.getKey();
            SyncFileInfo value = entry.getValue();
            SyncFileInfo info = clientMap.get(key);
            if (info == null) {     // [서버에만 파일이 있는 경우] 클라이언트에 데이터를 추가한다.
                System.out.println("[서버에만 파일이 있는 경우] ");
                Utils.sendMsg(socket, value.toJson("ADD"));
                Utils.sendFile(socket, Utils.findFileAtServer(value.getPath()));
            } else if (value.isModified(info)) {    // [서버, 클라이언트 둘 다 있지만 서버가 최신인 경우] 클라이언트에 데이터를 변경한다.
                System.out.println("[서버, 클라이언트 둘 다 있지만 서버가 최신인 경우]");
                Utils.sendMsg(socket, value.toJson("MODIFY"));
                Utils.sendFile(socket, Utils.findFileAtServer(value.getPath()));
            }
        }
    }

    private static Thread eventHandler(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        // 이벤트 감지
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String jsonStr = dis.readUTF();
                    SyncFileInfo sfi = SyncFileInfo.jsonToObject(jsonStr);
                    if (sfi == null) {
                        continue;
                    }
                    if (sfi.getRequest().equals("ADD")) {
                        fileAdd(socket, sfi);
                    } else if (sfi.getRequest().equals("MODIFY")) {
                        fileModify(socket, sfi);
                    } else if (sfi.getRequest().equals("DELETE")) {
                        fileDelete(sfi);
                    }
                }
            } catch (Exception e) {
                System.out.println("[예외 발생1] " + e.getMessage());
            } finally {
                try {
                    Print.stopWithClient(socket);
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        thread.start();
        return thread;
    }

    private static void fileAdd(Socket socket, SyncFileInfo sfi) throws IOException, ParseException {
        System.out.println("---------- ADD ----------");
        System.out.println("file: " + Const.BACKUP_PATH + sfi.getPath());
        File file = Utils.createDirectoryAndFileAtServer(sfi.getParentPath(), sfi.getPath());
        Utils.recvFile(socket, file);
        Utils.setLastModifiedDate(sfi.getLastModifiedDate(), file);
        prevMap.put(sfi.getPath(), sfi);
    }

    private static void fileModify(Socket socket, SyncFileInfo sfi) throws IOException, ParseException {
        System.out.println("---------- MODIFY ----------");
        System.out.println("file: " + Const.BACKUP_PATH + sfi.getPath());
        File file = Utils.findFileAtServer(sfi.getPath());
        file.delete();
        file = Utils.createDirectoryAndFileAtServer(sfi.getParentPath(), sfi.getPath());
        Utils.recvFile(socket, file);
        Utils.setLastModifiedDate(sfi.getLastModifiedDate(), file);
        prevMap.put(sfi.getPath(), sfi);
    }

    private static void fileDelete(SyncFileInfo sfi) {
        System.out.println("---------- DELETE ----------");
        System.out.println("file: " + Const.BACKUP_PATH + sfi.getPath());
        File file = Utils.findFileAtServer(sfi.getPath());
        file.delete();
        prevMap.remove(sfi.getPath());
    }

}
