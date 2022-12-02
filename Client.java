import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private static Map<String, SyncFileInfo> prevMap = new HashMap<>();

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket(Const.SERVER_NAME, Const.SERVER_PORT);

        Print.clientConnect(socket);

        // 특정 디렉토리 : ~/sync
        AllFileDirSearch search = new AllFileDirSearch(Const.CLIENT_SYNC_DIR);
        prevMap = search.getClientSyncFileInfos();
        Print.MapStringSyncFileInfo(prevMap);

        // 서버의 현재 backup 되어있는 파일들의 정보를 가져온다.
        Map<String, SyncFileInfo> serverMap = Utils.recvMapStringSyncFileInfo(socket);

        // 처음 클라이언트에 sync 되어있는 파일들의 정보를 서버한테 보낸다.
        Utils.sendMapStringSyncFileInfo(socket, prevMap);

        // 서버로 부터 추가, 변경, 삭제에 대한 요청 감지
        eventHandler(socket);

        clientFileCheck(socket, serverMap);

        // 클라이언트의 특정 디렉토리에서 파일의 추가, 변경, 삭제를 모니터링
        clientMonitoring(socket);

    }

    private static void clientMonitoring(Socket socket) {
        try {
            while (true) {
                Thread.sleep(1000 * 5);
                AllFileDirSearch search = new AllFileDirSearch(Const.CLIENT_SYNC_DIR);
                Map<String, SyncFileInfo> currMap = search.getClientSyncFileInfos();

                // prevMap 반복하면서 key값으로 currMap에서 찾는데 null이면 삭제된 파일 찾기.
                findDeletedFile(socket, currMap);

                // currMap 반복하면서 key값으로 prevMap에서 찾는데 null이면 추가된 파일 찾기.
                // currMap 반복하면서 key값으로 마지막 수정일을 비교해서 변경된 파일 찾기.
                findAddModifyFile(socket, currMap);

            }
        } catch (Exception e) {
            System.out.println("[예외 발생] " + e.getMessage());
        } finally {
            try {
                Print.stopWithServer(socket);
                socket.close();
            } catch (IOException ex) {
                System.out.println("[예외 발생] " + ex.getMessage());
            }
        }

    }

    private static void findAddModifyFile(Socket socket, Map<String, SyncFileInfo> currMap) throws IOException {
        for (Map.Entry<String, SyncFileInfo> entry : currMap.entrySet()) {
            String key = entry.getKey();
            SyncFileInfo value = entry.getValue();
            SyncFileInfo info = prevMap.get(key);
            if (info == null) {     // 추가된 파일
                System.out.println("[추가된 파일] " + Const.SYNC_PATH + value.getPath());
                Utils.sendMsg(socket, value.toJson("ADD"));
                Utils.sendFile(socket, Utils.findFileAtClient(value.getPath()));
                prevMap.put(key, value);
            } else if (value.isModified(info)) {    // 변경된 파일
                System.out.println("[변경된 파일] " + Const.SYNC_PATH + value.getPath());
                Utils.sendMsg(socket, value.toJson("MODIFY"));
                Utils.sendFile(socket, Utils.findFileAtClient(value.getPath()));
                prevMap.put(key, value);
            }
        }
    }

    private static void findDeletedFile(Socket socket, Map<String, SyncFileInfo> currMap) throws IOException {
        Map<String, SyncFileInfo> tempMap = Map.copyOf(prevMap);
        for (Map.Entry<String, SyncFileInfo> entry : tempMap.entrySet()) {
            String key = entry.getKey();
            SyncFileInfo value = entry.getValue();
            SyncFileInfo info = currMap.get(key);
            if (info == null) {     // 삭제된 파일
                System.out.println("[삭제된 파일] " + Const.SYNC_PATH + value.getPath());
                Utils.sendMsg(socket, value.toJson("DELETE"));
                prevMap.remove(key);
            }
        }
    }

    private static void clientFileCheck(Socket socket, Map<String, SyncFileInfo> serverMap) throws IOException {
        for (Map.Entry<String, SyncFileInfo> entry : prevMap.entrySet()) {
            String key = entry.getKey();
            SyncFileInfo value = entry.getValue();
            SyncFileInfo info = serverMap.get(key);
            if (info == null) {     // [클라이언트에만 파일이 있는 경우] 서버에 데이터를 추가한다.
                System.out.println("[클라이언트에만 파일이 있는 경우]");
                Utils.sendMsg(socket, value.toJson("ADD"));
                Utils.sendFile(socket, Utils.findFileAtClient(value.getPath()));
            } else if (value.isModified(info)) {    // [서버, 클라이언트 둘 다 있지만 클라이언트가 최신인 경우] 서버에 데이터를 변경한다.
                System.out.println("[서버, 클라이언트 둘 다 있지만 클라이언트가 최신인 경우]");
                Utils.sendMsg(socket, value.toJson("MODIFY"));
                Utils.sendFile(socket, Utils.findFileAtClient(value.getPath()));
            }
        }
    }

    private static void eventHandler(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        // 이벤트 감지
        Thread thread = new Thread(() -> {

            try {
                while (true) {
                    String jsonStr = dis.readUTF();
                    if (jsonStr.equals("END")) {
                        break;
                    }
                    SyncFileInfo sfi = SyncFileInfo.jsonToObject(jsonStr);
                    if (sfi == null) {
                        continue;
                    }
                    if (sfi.getRequest().equals("ADD")) {
                        System.out.print("[ADD] ");
                        System.out.println("file: " + Const.SYNC_PATH + sfi.getPath());
                        File file = Utils.createDirectoryAndFileAtClient(sfi.getParentPath(), sfi.getPath());
                        Utils.recvFile(socket, file);
                        Utils.setLastModifiedDate(sfi.getLastModifiedDate(), file);
                        prevMap.put(sfi.getPath(), sfi);
                    } else if (sfi.getRequest().equals("MODIFY")) {
                        System.out.print("[MODIFY] ");
                        System.out.println("file: " + Const.SYNC_PATH + sfi.getPath());
                        File file = Utils.findFileAtClient(sfi.getPath());
                        file.delete();
                        file = Utils.createDirectoryAndFileAtClient(sfi.getParentPath(), sfi.getPath());
                        Utils.setLastModifiedDate(sfi.getLastModifiedDate(), file);
                        Utils.recvFile(socket, file);
                        prevMap.put(sfi.getPath(), sfi);
                    } else if (sfi.getRequest().equals("DELETE")) {
                        System.out.print("[DELETE] ");
                        System.out.println("file: " + Const.SYNC_PATH + sfi.getPath());
                        File file = Utils.findFileAtClient(sfi.getPath());
                        file.delete();
                        prevMap.remove(sfi.getPath());
                    }

                }
                // System.out.println("Thread 종료 " + Thread.currentThread().getName());
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }

        });
        thread.start();
    }


}
