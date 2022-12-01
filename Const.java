import java.io.File;

public interface Const {
    String SERVER_NAME = "127.0.0.1";
//    String SERVER_NAME = "210.57.254.62";   // home

//    String SERVER_NAME = "15.165.161.152";    // ec2
    int SERVER_PORT = 50001;
    String HOME_DIR = System.getProperty("user.home");
    String SERVER_SYNC_DIR = "backup";
    String CLIENT_SYNC_DIR = "sync";
    String BACKUP_PATH = HOME_DIR + File.separator + SERVER_SYNC_DIR + File.separator;
    String SYNC_PATH = HOME_DIR + File.separator + CLIENT_SYNC_DIR + File.separator;
}
