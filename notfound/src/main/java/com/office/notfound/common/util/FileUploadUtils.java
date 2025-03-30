package com.office.notfound.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileUploadUtils {


    public static Map<String, String> saveStoreFile(String uploadDir, String fileName,
                                                    MultipartFile storeThumbnail,
                                                    MultipartFile storeImg1,
                                                    MultipartFile storeImg2,
                                                    MultipartFile storeImg3) throws IOException {

        Map<String, String> fileUrls = new HashMap<>();
        String savePath = "C:/upload/store";
        File dir = new File(savePath);

        // 디렉토리가 존재하지 않으면 생성
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 각 파일을 개별적으로 저장
        fileUrls.put("storeThumbnailUrl", saveFile(dir, fileName, storeThumbnail, "thumb_"));
        fileUrls.put("storeImg1Url", saveFile(dir, fileName, storeImg1, "img1_"));
        fileUrls.put("storeImg2Url", saveFile(dir, fileName, storeImg2, "img2_"));
        fileUrls.put("storeImg3Url", saveFile(dir, fileName, storeImg3, "img3_"));

        return fileUrls; // 저장된 파일 경로 반환
    }
    // 개별 파일 저장을 위한 메서드
    private static String saveFile(File dir, String fileName, MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originFileName = file.getOriginalFilename();
        if (originFileName == null || !originFileName.contains(".")) {
            return null; // 확장자가 없으면 저장하지 않음
        }

        String ext = originFileName.substring(originFileName.lastIndexOf("."));
        String savedFileName = prefix + fileName + ext;  // 파일명에 prefix 추가
        File target = new File(dir, savedFileName);
        file.transferTo(target);

        return "/image/store/" + savedFileName; // 저장된 경로 반환
    }

    public static boolean deleteStoreFile(String uploadDir, String fileName) {
        System.out.println("--- 이미지 삭제 시작 ---");
        System.out.println("fileDir = " + uploadDir);
        System.out.println("fileName = " + fileName);

        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("파일명이 비어있습니다.");
            return false;
        }

        // 업로드 디렉토리에서 파일 경로 생성
        Path filePath = Paths.get(uploadDir, fileName);

        try {
            // 파일 존재 여부 확인 후 삭제
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("이미지 삭제 성공: " + filePath.toString());
                return true;
            } else {
                System.out.println("삭제할 파일이 존재하지 않습니다: " + filePath.toString());
            }
        } catch (IOException e) {
            System.out.println("이미지 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } catch (SecurityException e) {
            System.out.println("파일 삭제 권한이 없습니다: " + e.getMessage());
        }

        return false;
    }

    public static String saveOfficeFile(String uploadDir, String fileName,
                                                    MultipartFile officeThumbnail) throws IOException {

        /* 프로젝트 루트 디렉토리의 절대 경로를 가져옴 */
        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/img/store";

        /* build 폴더 경로 생성 */
        String buildPath = projectPath + "/build/resources/main/static/img/store";
        File dir = new File(uploadDir);

        // 디렉토리가 존재하지 않으면 생성
        if (!dir.exists()) {
            dir.mkdirs();
        }

        /* 원본 파일 이름 가져오기 */
        String originFileName = officeThumbnail.getOriginalFilename();

        if (originFileName == null || originFileName.isEmpty()) {
            throw new IOException("파일이 존재하지 않거나 유효하지 않습니다.");
        }

        /* 확장자 추출 */
        String ext = originFileName.substring(originFileName.lastIndexOf("."));

        /* 저장할 파일 이름 생성 */
        String savedFileName = fileName + ext;

        /* 저장할 파일 경로 생성 */
        File target = new File(dir, savedFileName);

        /* 파일 저장 */
        officeThumbnail.transferTo(target);

        /* 저장된 파일 이름 반환 */
        return "/image/store/" + savedFileName;
    }

    public static boolean deleteOfficeFile(String uploadDir, String fileName) {
        System.out.println("--- 이미지 삭제 시작 ---");
        System.out.println("fileDir = " + uploadDir);
        System.out.println("fileName = " + fileName);

        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("파일명이 비어있습니다.");
            return false;
        }

        // 업로드 디렉토리에서 삭제할 파일 경로 생성
        Path filePath = Paths.get(uploadDir, fileName);

        try {
            // 파일 존재 여부 확인 후 삭제
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("이미지 삭제 성공: " + filePath.toString());
                return true;
            } else {
                System.out.println("삭제할 파일이 존재하지 않습니다: " + filePath.toString());
            }
        } catch (IOException e) {
            System.out.println("이미지 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } catch (SecurityException e) {
            System.out.println("파일 삭제 권한이 없습니다: " + e.getMessage());
        }

        return false;
    }


    public static String saveReviewFile(String imageDir, String imageName, MultipartFile reviewThumbnail)
            throws IOException {

        /* 프로젝트 루트 디렉토리의 절대 경로를 가져옴 */
        String projectPath = System.getProperty("user.dir") + "/notfound";

        /* build 폴더 경로 생성 */
        String buildPath = projectPath + "/build/resources/main/static/img/review";
        File dir = new File(buildPath);

        /* 디렉토리가 존재하지 않으면 디렉토리 생성 */
        if (!dir.exists()) {
            dir.mkdirs();
        }

        /* 원본 파일 이름 가져오기 */
        String originFileName = reviewThumbnail.getOriginalFilename();

        /* 확장자 추출 */
        String ext = originFileName.substring(originFileName.lastIndexOf("."));

        /* 저장할 파일 이름 생성 */
        String savedFileName = imageName + ext;

        /* 저장할 파일 경로 생성 */
        File target = new File(dir, savedFileName);

        /* 파일 저장 */
        reviewThumbnail.transferTo(target);

        /* 저장된 파일 이름 반환 */
        return "/img/review/" + savedFileName;
    }

    public static boolean deleteReviewFile(String fileDir, String fileName) {
        System.out.println("--- 이미지 삭제 시작 ---");
        System.out.println("fileDir = " + fileDir);
        System.out.println("fileName = " + fileName);

        /* build 디렉토리 경로를 기반으로 삭제할 파일 경로 생성 */
        String projectPath = System.getProperty("user.dir");
        String buildPath = projectPath + "/build/resources/main/static/img/review";
        Path filePath = Paths.get(buildPath, fileName);

        try {
            /* 파일 존재 여부 확인 후 삭제 */
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("이미지 삭제 성공: " + filePath.toString());
                return true;
            } else {
                System.out.println("삭제할 파일이 존재하지 않습니다: " + filePath.toString());
            }
        } catch (IOException e) {
            System.out.println("이미지 삭제 중 오류 발생: " + e.getMessage());
        }

        return false;
    }

}