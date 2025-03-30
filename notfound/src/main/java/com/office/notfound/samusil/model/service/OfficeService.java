package com.office.notfound.samusil.model.service;

import com.office.notfound.common.util.FileUploadUtils;
import com.office.notfound.samusil.model.dao.OfficeMapper;
import com.office.notfound.samusil.model.dto.OfficeDTO;
import com.office.notfound.store.model.dao.StoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class OfficeService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final OfficeMapper officeMapper;
    private final StoreMapper storeMapper;

    @Autowired
    public OfficeService(OfficeMapper officeMapper, StoreMapper storeMapper) {
        this.officeMapper = officeMapper;
        this.storeMapper = storeMapper;
    }

    // store_code에 따른 사무실 리스트 전체 조회
    public List<OfficeDTO> findAllOffices(int storeCode) {
        // 전체조회긴 하지만 전달받은 파라미터에 해당하는 것만 전체조회
        return officeMapper.findAllOffices(storeCode);
    }

    public OfficeDTO findOfficeDetail(int officeCode) {
        OfficeDTO office = officeMapper.findOfficeDetail(officeCode);

        return office;
    }

    public OfficeDTO findOfficeByStore(int storeCode, int officeCode) {

        return officeMapper.findOfficeByStore(storeCode, officeCode);
    }

    @Transactional
    public void createOffice(OfficeDTO office, MultipartFile officeThumbnail) throws IOException {
        if (!officeThumbnail.isEmpty()) {
            // 파일 저장 후 경로 반환
            String savedFilePath = FileUploadUtils.saveOfficeFile(uploadDir, "office_" + System.currentTimeMillis(), officeThumbnail);
            office.setOfficeThumbnailUrl(savedFilePath);  // DB에 저장할 경로 설정
        }

        // DB에 Office 정보 저장
        officeMapper.insertOffice(office);
    }

    @Transactional
    public void updateOffice(OfficeDTO office, MultipartFile newImage) throws IOException {

        // 사무실 코드로 기존 사무실 정보 가져오기
        OfficeDTO existingOffice = officeMapper.findOfficeByCode(office.getOfficeCode());
        String oldImagePath = uploadDir + "/" + existingOffice.getOfficeThumbnailUrl();

        String newFileName = null; // 초기값 설정

        // 새로운 이미지가 업로드되었을 경우
        if (newImage != null && !newImage.isEmpty()) {
            String fileUrl = existingOffice.getOfficeThumbnailUrl();

            // URL이면 삭제를 SKIP하고, 로컬이면 삭제
            if (fileUrl != null && !fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
                // URL이 아닌 로컬파일일 때만 삭제
                boolean deleteResult = FileUploadUtils.deleteOfficeFile(uploadDir, fileUrl);
                if (!deleteResult) {
                    throw new IOException("기존 파일 삭제 실패");
                }
            }
            // 기존 이미지 삭제
            if (existingOffice.getOfficeThumbnailUrl() != null) {
                FileUploadUtils.deleteOfficeFile(uploadDir, existingOffice.getOfficeThumbnailUrl());
                System.out.println("[INFO] 기존 이미지 삭제 완료: " + oldImagePath);
            }
            // 새로운 이미지 저장
            newFileName = UUID.randomUUID().toString().replace("-", "") +
                    newImage.getOriginalFilename().substring(newImage.getOriginalFilename().lastIndexOf("."));
            File newFile = new File(uploadDir + "/" + newFileName);
            try {
                newImage.transferTo(newFile);
                System.out.println("[INFO] 새 이미지 저장 완료: " + newFile.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("[ERROR] 파일 저장 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("파일 저장에 실패했습니다.", e); // 예외 발생시 롤백 처리를 위해 필수
            }
        } else {
            // 새 이미지 없으면 원래 이미지로 유지
            office.setOfficeThumbnailUrl(existingOffice.getOfficeThumbnailUrl());
        }

        // DB에 새로운 이미지 정보 업데이트
        office.setOfficeThumbnailUrl(newFileName);

            // 파일 저장 경로 및 파일 이름 처리
//                String filePath = FileUploadUtils.saveOfficeFile("사무실 이미지 저장 경로", UUID.randomUUID().toString(), newImage);
//                office.setOfficeThumbnailUrl(filePath);
//            }

        // 오류 발생시 자동 롤백
        officeMapper.updateOffice(office);
    }

    @Transactional
    public void deleteOffice(int officeCode) throws Exception {

        OfficeDTO office = officeMapper.findOfficeByCode(officeCode);
        System.out.println("=== 서비스에서 받은 officeCode: " + officeCode);

        int result = officeMapper.deleteOffice(officeCode);

        if (result > 0) {
            System.out.println("사무실 삭제 성공!");
        } else {
            System.out.println("사무실 삭제 실패! officeCode: " + officeCode);
        }

        if (office == null) {
            throw new IllegalArgumentException("선택한 사무실이 존재하지 않습니다.");
        }

        // 이미지 파일 삭제 또는 DB에서 URL 제거
        if (office.getOfficeThumbnailUrl() != null && !office.getOfficeThumbnailUrl().isEmpty()) {
            String imageUrl = office.getOfficeThumbnailUrl();
            System.out.println("=== 삭제할 이미지 URL: " + imageUrl);


            if (imageUrl.startsWith("https")) {
                // URL만 저장된 경우 → DB에서 NULL로 업데이트
                officeMapper.deleteOfficeImageUrl(officeCode);
            } else {
                // 서버 내부 파일이면 삭제
                try {
                    // 연관 테이블 삭제
                    officeMapper.deleteOfficeRelated(officeCode);
                    // 메인 테이블 삭제
                    officeMapper.deleteOffice(officeCode);

//                    Path filePath = Paths.get(office.getOfficeThumbnailUrl());
//                    Files.deleteIfExists(filePath);
                    Path filePath = Paths.get(imageUrl);
                    Files.deleteIfExists(filePath);
                    System.out.println("=== 파일 삭제 완료: " + filePath);

                } catch (Exception e) {
                    throw new RuntimeException("사무실 삭제 중 오류가 발생했습니다.", e);
                }
            }

//                System.out.println("=== Service: 사무실 삭제 시도 ===");
//                System.out.println("삭제할 officeCode: " + officeCode);
////                // officeCode에 해당하는 사무실이 해당 store에 속하는지 검증
////                // 예를 들어: officeCode와 storeCode로 사무실을 삭제하는 경우
//                int deleteCount = officeMapper.deleteOffice(officeCode);
//                System.out.println("삭제된 사무실의 수: " + deleteCount);
//
//                if (deleteCount == 0) {
//                    throw new IllegalArgumentException("해당 사무실을 삭제할 수 없습니다.");
//                }
            officeMapper.deleteOffice(officeCode);
        }
    }

    public List<String> getOfficeTypes() {
        return officeMapper.getOfficeTypes();
    }

    public OfficeDTO findOfficeByCode(int officeCode) {

        OfficeDTO office = officeMapper.findOfficeByCode(officeCode);

        if(office != null) {
            office.setOfficeThumbnailUrl(office.getOfficeThumbnailUrl());
        }

        return office;
    }
}
