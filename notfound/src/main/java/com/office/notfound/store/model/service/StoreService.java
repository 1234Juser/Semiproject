package com.office.notfound.store.model.service;


import com.office.notfound.common.util.FileUploadUtils;
import com.office.notfound.store.model.dao.StoreMapper;
import com.office.notfound.store.model.dto.StoreDTO;
import org.apache.catalina.Store;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.office.notfound.common.util.FileUploadUtils.saveStoreFile;


@Service
public class StoreService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final StoreMapper storeMapper;

    @Autowired
    public StoreService(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    public List<StoreDTO> findAllStores() {
        List<StoreDTO> stores = storeMapper.findAllStores();
        return storeMapper.findAllStores();
    }

    public StoreDTO findStoreByCode(int storeCode) {
        StoreDTO store = storeMapper.findStoreByCode(storeCode);

        return storeMapper.findStoreByCode(storeCode);
    }

    @Transactional
    public void createStore(StoreDTO store, MultipartFile storeThumbnail, MultipartFile storeImg1, MultipartFile storeImg2, MultipartFile storeImg3) throws Exception {

        String fileName = UUID.randomUUID().toString().replace("-", ""); // 파일명 생성
        Map<String, String> fileUrls = saveStoreFile(uploadDir, fileName, storeThumbnail, storeImg1, storeImg2, storeImg3);

        // DTO에 저장된 이미지 URL 세팅
        store.setStoreThumbnailUrl(fileUrls.get("storeThumbnailUrl"));
        store.setStoreImg1Url(fileUrls.get("storeImg1Url"));
        store.setStoreImg2Url(fileUrls.get("storeImg2Url"));
        store.setStoreImg3Url(fileUrls.get("storeImg3Url"));

        // 상품 정보 저장
        storeMapper.insertStore(store);
    }


//    @Transactional
//    public void createStore(StoreDTO store, MultipartFile storeImg1) throws Exception {
//
//        // 각 이미지의 저장된 URL을 저장할 변수 선언
//        String img1Url = null;
//
//        // 이미지 저장
//        if (!storeImg1.isEmpty()) {
//            String fileName = UUID.randomUUID().toString().replace("-", "");
//            img1Url = FileUploadUtils.saveStoreFile(uploadDir, fileName, storeImg1);
//        }
//        store.setStoreImg1Url(img1Url);
//
//        // 상품 정보 저장
//        storeMapper.insertStore(store);
//    }
//
//    @Transactional
//    public void createStore(StoreDTO store, MultipartFile storeImg2) throws Exception {
//
//        // 각 이미지의 저장된 URL을 저장할 변수 선언
//        String img2Url = null;
//
//        // 이미지 저장
//        if (!storeImg2.isEmpty()) {
//            String fileName = UUID.randomUUID().toString().replace("-", "");
//            img2Url = FileUploadUtils.saveStoreFile(uploadDir, fileName, storeImg2);
//        }
//        store.setStoreImg2Url(img2Url);
//
//
//        // 상품 정보 저장
//        storeMapper.insertStore(store);
//    }
//
//    @Transactional
//    public void createStore(StoreDTO store, MultipartFile storeImg3) throws Exception {
//
//        // 각 이미지의 저장된 URL을 저장할 변수 선언
//        String img3Url = null;
//
//        // 이미지 저장
//
//        if (!storeImg3.isEmpty()) {
//            String fileName = UUID.randomUUID().toString().replace("-", "");
//            img3Url = FileUploadUtils.saveStoreFile(uploadDir, fileName, storeImg3);
//        }
//
//        store.setStoreImg3Url(img3Url);
//
//
//        // 상품 정보 저장
//        storeMapper.insertStore(store);
//    }

    public List<String> getDistinctCities() {

        return storeMapper.findDistinctCities();
    }


    public List<String> getGuByCity(String city) {

        return storeMapper.findGuByCity(city);
    }

    public List<StoreDTO> findStoresByCityAndGu(String city, String gu) {

        return storeMapper.findStoresByCityAndGu(city, gu);
    }

    @Transactional
    public void updateStore(StoreDTO store, MultipartFile newImage) throws IOException {

        // 지점 코드로 기존 지점 정보 가져오기
        StoreDTO existingStore = storeMapper.findStoreByCode(store.getStoreCode());
//        String oldImageName = existingStore.getStoreThumbnailUrl();
        String oldImagePath = uploadDir + "/" + existingStore.getStoreThumbnailUrl();

        String newFileName = null; // 초기값 설정

        // 새로운 이미지가 업로드되었을 경우
        if (newImage != null && !newImage.isEmpty()) {
            String fileUrl = existingStore.getStoreThumbnailUrl();

            // URL이면 삭제를 SKIP하고, 로컬이면 삭제
            if (fileUrl != null && !fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
                // URL이 아닌 로컬파일일 때만 삭제
                boolean deleteResult = FileUploadUtils.deleteStoreFile(uploadDir, fileUrl);
                if (!deleteResult) {
                    throw new IOException("기존 파일 삭제 실패");
                }
            }
            // 기존 이미지 삭제
            if (existingStore.getStoreThumbnailUrl() != null) {
                FileUploadUtils.deleteStoreFile(uploadDir, existingStore.getStoreThumbnailUrl());
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
            store.setStoreThumbnailUrl(existingStore.getStoreThumbnailUrl());

        }

        // DB에 새로운 이미지 정보 업데이트
        store.setStoreThumbnailUrl(newFileName);
        store.setStoreImg1Url(newFileName);
        store.setStoreImg2Url(newFileName);
        store.setStoreImg3Url(newFileName);

        // 오류 발생시 자동 롤백
        storeMapper.updateStore(store);
    }

    @Transactional
    public void deleteStore(int storeCode) {
        // 지점 이미지 정보 조회
        StoreDTO store = storeMapper.findStoreByCode(storeCode);

        if (store == null) {
            throw new IllegalArgumentException("선택 지점이 존재하지 않습니다.");
        }

        // 이미지 파일 삭제 또는 DB에서 URL 제거
        if (store.getStoreThumbnailUrl() != null && !store.getStoreThumbnailUrl().isEmpty()) {
            String imageUrl = store.getStoreThumbnailUrl();

            if (imageUrl.startsWith("https")) {
                // URL만 저장된 경우 → DB에서 NULL로 업데이트
                storeMapper.deleteStoreImageUrl(storeCode);
            } else {
                // 서버 내부 파일이면 삭제
                try {
                    Path filePath = Paths.get(imageUrl);
                    Files.deleteIfExists(filePath);
                } catch (Exception e) {
                    throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다.", e);
                }
            }
            // 지점 삭제
            storeMapper.deleteStore(storeCode);
        }
    }

    public StoreDTO findStoreByName(String storeName) {
        return storeMapper.findStoreByName(storeName);
    }
}