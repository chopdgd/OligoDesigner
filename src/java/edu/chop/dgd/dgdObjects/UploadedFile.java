package edu.chop.dgd.dgdObjects;

/**
 * Created by jayaramanp on 6/1/14.
 */
import org.springframework.web.multipart.MultipartFile;

public class UploadedFile {

        private MultipartFile file;

        public MultipartFile getFile() {
            return file;
        }

        public void setFile(MultipartFile file) {
            this.file = file;
        }
}
