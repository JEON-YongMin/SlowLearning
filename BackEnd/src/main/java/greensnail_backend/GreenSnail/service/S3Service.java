package greensnail_backend.GreenSnail.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.bucket}")
    private String bucketName;

    /**
     * S3에 파일 업로드 (ACL 사용 안함)
     */
    public String uploadFile(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "파일이 비어 있습니다.");
        }

        String fileName = createFileName(file.getOriginalFilename());
        String filePath = directory + "/" + fileName;

        try {
            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드 (ACL 설정 없이)
            amazonS3.putObject(
                    new PutObjectRequest(bucketName, filePath, file.getInputStream(), metadata)
            );

            // 업로드된 파일의 S3 URL 반환
            String fileUrl = amazonS3.getUrl(bucketName, filePath).toString();
            log.info("파일 업로드 완료: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        } catch (Exception e) {
            log.error("S3 서비스 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // URL에서 파일 키 추출
            String fileKey = fileUrl.replace(amazonS3.getUrl(bucketName, "").toString(), "");

            // S3에서 파일 삭제
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileKey));
            log.info("파일 삭제 완료: {}", fileKey);
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * 고유한 파일명 생성
     */
    private String createFileName(String originalFilename) {
        return UUID.randomUUID().toString() + extractFileExtension(originalFilename);
    }

    /**
     * 파일 확장자 추출
     */
    private String extractFileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "";
        }

        int extensionPosition = originalFilename.lastIndexOf(".");
        if (extensionPosition == -1) {
            return "";
        }

        return originalFilename.substring(extensionPosition);
    }
}