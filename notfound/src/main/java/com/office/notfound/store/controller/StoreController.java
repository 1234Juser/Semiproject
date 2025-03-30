package com.office.notfound.store.controller;

import com.office.notfound.review.model.dto.OfficeReviewDTO;
import com.office.notfound.review.model.service.ReviewService;
import com.office.notfound.samusil.model.dto.OfficeDTO;
import com.office.notfound.samusil.model.service.OfficeService;
import com.office.notfound.store.model.dto.StoreDTO;
import com.office.notfound.store.model.service.StoreService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.List;

@Controller
public class StoreController {


    private final StoreService storeService;
    private final OfficeService officeService;
    private final ReviewService reviewService;

    @Autowired
    public StoreController(StoreService storeService, OfficeService officeService, ReviewService reviewService) {
        this.storeService = storeService;
        this.officeService = officeService;
        this.reviewService = reviewService;
    }

    // 전체 지점 조회
    @GetMapping("/store/storelist")
    public String storeList(Model model) {

        List<StoreDTO> storeList = storeService.findAllStores();

        model.addAttribute("storeList", storeList);

        return "/store/storelist";
    }

    // 특정 지점 상세 조회 후 해당 사무실 리스트 전체 조회
    @GetMapping("/store/detailstore/{storeCode}")
    public String storeDetail(@PathVariable("storeCode") int storeCode, Model model) {
        // storeCode에 해당하는 매장 정보 조회
        StoreDTO store = storeService.findStoreByCode(storeCode);

        List<OfficeDTO> officeList = officeService.findAllOffices(storeCode);
        // 사무실 상세 리스트 내 리뷰 조회용
        List<OfficeReviewDTO> FindOfficeReview = reviewService.findOfficeReview(storeCode);


        // 모델에 해당 매장 정보를 담아 상세 페이지를 반환
        model.addAttribute("store", store);
        model.addAttribute("latitude", store.getLatitude());
        model.addAttribute("longitude", store.getLongitude());
        model.addAttribute("officeList", officeList);
        model.addAttribute("FindOfficeReview", FindOfficeReview);

        return "store/detailstore";
    }

    // 관리자페이지>매장관리의 디폴트화면은 전체지점리스트
    // 관리자용 상품 목록
    @GetMapping("/store/admin/storemanage")
    public ModelAndView adminStoreList(ModelAndView mv) {

        List<StoreDTO> stores = storeService.findAllStores();

        mv.addObject("stores", stores);
        mv.setViewName("store/admin/storemanage");

        return mv;
    }

    // 관리자용 상품 등록 페이지
    @GetMapping("store/admin/storecreate")
    public String adminStoreCreatePage() {
        return "store/admin/storecreate";
    }

    // 관리자용 상품 등록 처리
    @PostMapping("store/admin/storecreate")
    public String adminStoreCreate(@ModelAttribute StoreDTO store,
                                   @RequestParam("storeThumbnail") MultipartFile storeThumbnail,
                                   @RequestParam(value = "storeImg1", required = false) MultipartFile storeImg1,
                                   @RequestParam(value = "storeImg2", required = false) MultipartFile storeImg2,
                                   @RequestParam(value = "storeImg3", required = false) MultipartFile storeImg3,
                                   RedirectAttributes rttr) {


        try {
            storeService.createStore(store, storeThumbnail, storeImg1, storeImg2, storeImg3);
            rttr.addFlashAttribute("message", "새 지점 등록을 성공하였습니다.");
            // 지점 등록 성공 후 이동하는 페이지는 디폴트
            return "redirect:/store/admin/storemanage";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("message", e.getMessage());
            return "redirect:/store/admin/storecreate";
        } catch (Exception e) {
            rttr.addFlashAttribute("message", "새 지점 등록에 실패했습니다: " + e.getMessage());
            return "redirect:/store/admin/storecreate";
        }
    }


    // 지역별 지점 조회 페이지
    @GetMapping("/store/storeregion")
    public String storeRegionPage(
            @RequestParam(required = false) String storeCity,
            @RequestParam(required = false) String storeGu,
            Model model) {

//        List<Store> storesRegion = storeService.findStoresByRegion(city, gu);
//        model.addAttribute("storeRegionPage", storesRegion); // 여기에 리스트 저장
        return "store/storeregion";
    }

    // 시(city) 정보 조회
    @GetMapping("/store/storeregion/cities")
    @ResponseBody
    public List<String> getStoreCities() {
        return storeService.getDistinctCities();
    }

    // 시 선택 시 해당 구/군 정보 조회
    @GetMapping("/store/storeregion/gu")
    @ResponseBody
    public List<String> getGuByCity(@RequestParam("city") String city) {
        return storeService.getGuByCity(city);
    }

    @GetMapping("/store/storeregion/search")
    @ResponseBody
    public List<StoreDTO> getStoresByRegion(@RequestParam(required = false) String city,  // city는 optional
                                            @RequestParam String gu) {  // gu는 필수값
        System.out.println("선택한 지역구: " + gu);
        List<StoreDTO> stores = storeService.findStoresByCityAndGu(city, gu);
        System.out.println("결과 스토어 리스트: " + stores); // 디버깅용 출력
        return stores;
    }

    // 지점 수정 페이지 넘어가기
    @GetMapping("/store/admin/storeedit/{storeCode}")
    public String adminStoreEditPage(@PathVariable("storeCode") int storeCode, Model model) {

        StoreDTO store = storeService.findStoreByCode(storeCode);

        if (store == null) {
            throw new IllegalArgumentException("해당 StoreCode에 해당하는 지점 정보가 없습니다."); // 데이터 없을 경우 예외 처리
        }

        model.addAttribute("store", store);

        return "store/admin/storeedit";
    }

    // 수정된 지점 정보 저장하기
    @PostMapping("/store/admin/storeedit/{storeCode}")
    public String adminStoreEdit(@PathVariable("storeCode") int storeCode,
                                 @ModelAttribute StoreDTO store,
                                 @RequestParam(value = "newImage", required = false) MultipartFile newImage,
                                 RedirectAttributes rttr) {

        try {
            if (store.getStoreName() == null || store.getStoreAddress() == null) {
                throw new IllegalArgumentException("필수 입력 데이터가 누락되었습니다.");
            }

            store.setStoreCode(storeCode);
            storeService.updateStore(store, newImage);
            rttr.addFlashAttribute("message", "지점 정보 수정을 성공하였습니다.");
            // 지점 수정 성공 후 이동하는 페이지는 디폴트
            return "redirect:/store/admin/storemanage";
        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("message", "지점 정보 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/store/admin/storeedit/" + storeCode;
        }
    }

    // 지점 삭제하기
    @PostMapping("/store/admin/delete/{storeCode}")
    public String adminStoreDelete(@PathVariable("storeCode") int storeCode,
                                   RedirectAttributes rttr) {

        try {
            storeService.deleteStore(storeCode);
            rttr.addFlashAttribute("message", "선택한 지점이 삭제되었습니다.");
            return "redirect:/store/admin/storemanage";
        } catch (Exception e) {
            rttr.addFlashAttribute("message", "선택 지점 삭제에 실패했습니다: " + e.getMessage());
            return "redirect:/store/admin/storemanage";
        }
    }

    @PostMapping("/store/admin/storemanage/{storeCode}")
    public String deleteStore(@PathVariable("storeCode") int storeCode) {
        storeService.deleteStore(storeCode);
        return "redirect:/store/admin/storemanage";
    }

    @GetMapping("/stores/store-code")
    @ResponseBody
    public int getStoreCodeByName(@RequestParam String storeName) {
        StoreDTO store = storeService.findStoreByName(storeName);
        return store != null ? store.getStoreCode() : -1;
    }

}
