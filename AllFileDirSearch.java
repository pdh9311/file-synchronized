import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllFileDirSearch {

    private String backupDir;
    private List<File> dirList = new ArrayList<>();
    private List<File> fileList = new ArrayList<>();
    private Map<String, SyncFileInfo> clientSyncFileInfos = new HashMap<>();

    public AllFileDirSearch(String dir) {
        this.backupDir = Const.HOME_DIR + File.separator + dir;

        File file = new File(backupDir);
        if (file.exists() == false) {
            file.mkdir();
        }
        searchSubDir(file.getParent(), file.getName());
        createSyncFileInfoList();
    }

    public List<File> getDirList() {
        return dirList;
    }

    public List<File> getFileList() {
        return fileList;
    }

    public Map<String, SyncFileInfo> getClientSyncFileInfos() {
        return clientSyncFileInfos;
    }

    private void searchSubDir(String parentDir, String filename) {
       /* System.out.print("=========== parentDir : " + parentDir);
        System.out.println("\t|\tfilename = " + filename + " ===========");*/
        File file = new File(parentDir + File.separator + filename);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                dirList.add(f);
                searchSubDir(f.getParent(), f.getName());
            } else {
                fileList.add(f);
            }
        }
    }

    private String getFilDirPath(String parentPath, String findStr) {
        int removeEndIdx = parentPath.indexOf(findStr) + findStr.length();
        String removeStr = parentPath.substring(0, removeEndIdx);
        String result = parentPath.replace(removeStr, "");
        if (result.length() != 0) {
            return result.substring(1);
        }
        return result;
    }

    private void createSyncFileInfoList() {
        for (int i = 0; i < fileList.size(); i++) {
            String dirPath = getFilDirPath(fileList.get(i).getParent(), backupDir);
            SyncFileInfo sfi = new SyncFileInfo(dirPath, fileList.get(i).getName(), fileList.get(i).lastModified());
            clientSyncFileInfos.put(sfi.getPath(), sfi);
        }
    }

    @Override
    public String toString() {
        return "AllFileDirSearch{" +
                "dirList=" + dirList.size() +
                ", fileList=" + fileList.size() +
                ", clientSyncFileInfos=" + clientSyncFileInfos.size() +
                '}';
    }
}
