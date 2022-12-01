import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * parentDir : backup 디렉토리 기준 "디렉토리 경로"
 * fileName : 파일명
 * lastModifiedDate : 마지막 수정 날짜 yyyy-MM-dd hh:mm:ss
 * path : dirPath + fileName
 */
@Getter
@Setter
public class SyncFileInfo {
    private String request;
    private String parentPath;
    private String fileName;
    private String lastModifiedDate;
    private String path;

    public SyncFileInfo() {
    }

    public SyncFileInfo(String parentPath, String fileName, long lastModifiedDate) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(lastModifiedDate));
        if (parentPath.equals("")) {
            this.path = parentPath + fileName;
        } else {
            this.path = parentPath + File.separator + fileName;
        }
    }

    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.out.println("[JsonProcessingException] " + e.getMessage());
        }
        return null;
    }

    public String toJson(String request) {
        this.request = request;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.out.println("[JsonProcessingException] " + e.getMessage());
        }
        return null;
    }


    public static SyncFileInfo jsonToObject(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, SyncFileInfo.class);
        } catch (JsonProcessingException e) {
            System.out.println("[JsonProcessingException] : " + json + " : " + e.getMessage());
        }
        return null;
    }

    public boolean isModified(SyncFileInfo clientSFI) {
        String str1 = lastModifiedDate;
        String str2 = clientSFI.getLastModifiedDate();

        LocalDateTime ldt1 = LocalDateTime.parse(str1, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime ldt2 = LocalDateTime.parse(str2, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (ldt1.compareTo(ldt2) > 0) {
            return true;
        }
        return false;
    }

}
