import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static void sendInt(Socket socket, int i) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(i);
        dos.flush();
    }

    public static int recvInt(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        return dis.readInt();
    }

    public static void sendMsg(Socket socket, String msg) throws IOException {
        System.out.println("[send]" + msg);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(msg);
        dos.flush();
    }

    public static String recvMsg(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        String msg = dis.readUTF();
        System.out.println("[recv]" + msg);
        return msg;
    }

    public static void sendFile(Socket socket, File file) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] data = new byte[2048];
        int len;
        while ((len = bis.read(data)) != -1) {
            dos.writeInt(len);              // 읽은 바이트 길이
            dos.flush();
            dos.write(data, 0, len);    // 읽은 바이트
            dos.flush();
        }
        dos.writeInt(-1);
        dos.flush();
        bis.close();
    }

    public static void recvFile(Socket socket, File file) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] data = new byte[2048];
        int len;
        while ((len = dis.readInt()) != -1) {       // 읽은 바이트 길이
            dis.read(data, 0, len);
            bos.write(data, 0, len);
            bos.flush();
        }
        bos.close();
    }

    public static void sendMapStringSyncFileInfo(Socket socket, Map<String, SyncFileInfo> map) throws IOException {
        sendInt(socket, map.size());
        for (Map.Entry<String, SyncFileInfo> entry : map.entrySet()) {
            String jsonStr = entry.getValue().toJson();
            sendMsg(socket, jsonStr);
        }
    }

    public static Map<String, SyncFileInfo> recvMapStringSyncFileInfo(Socket socket) throws IOException {
        Map<String, SyncFileInfo> result = new HashMap<>();
        int len = recvInt(socket);
        for (int i = 0; i < len; i++) {
            String jsonStr = recvMsg(socket);
            SyncFileInfo sfi = SyncFileInfo.jsonToObject(jsonStr);
            result.put(sfi.getPath(), sfi);
        }
        return result;
    }

    public static File findFileAtServer(String filePath) {
        String path = Const.BACKUP_PATH + filePath;
        return new File(path);
    }

    public static File createDirectoryAndFileAtServer(String directory, String filePath) throws IOException {
        String parentPath = Const.BACKUP_PATH + directory;
        String path = Const.BACKUP_PATH + filePath;

        File parent = new File(parentPath);
        if (!parent.exists()) {
            parent.mkdirs();
        }

        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static File findFileAtClient(String filePath) {
        String path = Const.SYNC_PATH + filePath;
        return new File(path);
    }

    public static File createDirectoryAndFileAtClient(String directory, String filePath) throws IOException {
        String parentPath = Const.SYNC_PATH + directory;
        String path = Const.SYNC_PATH + filePath;

        File parent = new File(parentPath);
        if (!parent.exists()) {
            parent.mkdirs();
        }

        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    // 파일의 마지막 수정 날짜를 직접 변경
    public static void setLastModifiedDate(String dateTime, File file) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = formatter.parse(dateTime);
        file.setLastModified(date.getTime());
    }

}
