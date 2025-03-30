package com.office.notfound.samusil.controller;

import com.office.notfound.common.util.FileUploadUtils;
import com.office.notfound.samusil.model.dto.OfficeDTO;
import com.office.notfound.samusil.model.service.OfficeService;
import com.office.notfound.store.model.dto.StoreDTO;
import com.office.notfound.store.model.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;
import java.util.UUID;

@Controller
public class OfficeController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final OfficeService officeService;
    private final StoreService storeService;

    @Autowired
    public OfficeController(OfficeService officeService, StoreService storeService) {
        this.officeService = officeService;
        this.storeService = storeService;
    }

    // 사무실 전체 조회
    @GetMapping("/store/detailstore")
    public String officeList(@RequestParam("storeCode") int storeCode, Model model) {
        StoreDTO store = storeService.findStoreByCode(storeCode);

        List<OfficeDTO> officeList = officeService.findAllOffices(storeCode);

        model.addAttribute("store", store);
        model.addAttribute("officeList", officeList);

        // 반환할 뷰 aka 보여줄 html파일 작성
        return "/store/detailstore";
    }

    @GetMapping("/store/detailoffice/{storeCode}/{officeCode}")
    public String findOfficeDetail(@PathVariable("storeCode") int storeCode,
                                   @PathVariable("officeCode") int officeCode,
                                   Model model) {

        // Store 정보 조회 (단일 객체 반환)
        StoreDTO store = storeService.findStoreByCode(storeCode);
        // Office 정보 조회 (단일 객체 반환)
        OfficeDTO office = officeService.findOfficeDetail(officeCode);

        // office가 null인지 확인 (오류 방지)
        if (office == null) {
            System.out.println("❌ Office 객체가 null입니다. officeCode: " + officeCode);
            model.addAttribute("errorMessage", "해당 사무실 정보를 찾을 수 없습니다.");
            return "error-page"; // 에러 페이지로 이동
        }

        // Debug 로그 추가 (콘솔에서 데이터 확인)
        System.out.println("✅ [DEBUG] storeCode: " + storeCode + ", officeCode: " + officeCode);
        System.out.println("✅ [DEBUG] 조회된 Office 정보: " + office);

        // 모델에 데이터를 추가
        model.addAttribute("store", store);
        model.addAttribute("office", office);

        // 예약 등록 페이지로 리다이렉트
        return "redirect:/reservation/register?storeCode=" + storeCode + "&officeCode=" + officeCode;
    }

    @GetMapping("/samusil/admin/officemanage/{storeCode}")
    public String adminOfficeList(@PathVariable("storeCode") int storeCode, Model model) {
        StoreDTO store = storeService.findStoreByCode(storeCode);
        List<OfficeDTO> officeList = officeService.findAllOffices(storeCode);

        model.addAttribute("officeList", officeList);
        model.addAttribute("store", store);

        // 반환할 뷰 aka 보여줄 html파일 작성
        return "samusil/admin/officemanage";
    }

    // 관리자용 사무실 등록 페이지
    @GetMapping("/samusil/admin/officecreate")
    public String adminOfficeCreatePage(Model model) {

        List<String> typeList = officeService.getOfficeTypes(); // 기존에 있는 서비스 메서드 활용
        List<StoreDTO> storeList = storeService.findAllStores(); // 기존의 findAllStores() 호출. store_name과 store_code 리스트 가져오기

        model.addAttribute("typeList", typeList);
        model.addAttribute("storeList", storeList);

        return "/samusil/admin/officecreate";
    }

    // 사무실 등록 처리하기
    @PostMapping("/samusil/admin/officecreate")
    public String adminOfficeCreate(
            @Valid OfficeDTO office,
            @RequestParam("storeCity") String storeCity,
            @RequestParam("storeGu") String storeGu,
            @RequestParam("storeName") String storeName,
            @RequestParam("officeType") String officeType,
            @RequestParam("officeNum") int officeNum,
            @RequestParam("officePrice") int officePrice,
            @RequestParam("officeThumbnail") MultipartFile officeThumbnail,
            RedirectAttributes rAttr) {

        // storeCode를 Integer로 받고, null을 처리
        Integer storeCode = Integer.valueOf(storeName);  // storeName은 실제로 storeCode를 나타냅니다.
        office.setStoreCode(storeCode);

        if (office.getStoreCode() == 0) { // storeCode가 0이라면 빈 값이 들어왔다는 의미
            rAttr.addFlashAttribute("error", "지점은 필수 선택입니다.");
            return "redirect:/samusil/admin/officecreate";
        }

        try {
            officeService.createOffice(office, officeThumbnail);
            rAttr.addFlashAttribute("message", "새 사무실 등록을 성공하였습니다.");
            // 사무실 등록 성공 후 이동하는 페이지는 디폴트
            return "redirect:/samusil/admin/officemanage/" + storeCode;
        } catch (IllegalArgumentException e) {
            rAttr.addFlashAttribute("message", e.getMessage());
            return "redirect:/samusil/admin/officemanage/" + storeCode;
        } catch (Exception e) {
            rAttr.addFlashAttribute("message", "새 사무실 등록에 실패했습니다: " + e.getMessage());
            return "redirect:/samusil/admin/officecreate";
        }

    }

    // 지점별 사무실 삭제
    @PostMapping("/samusil/admin/delete/{storeCode}/{officeCode}")
    public String adminOfficeDelete(@PathVariable("storeCode") int storeCode,
                                    @PathVariable("officeCode") int officeCode,
                                    RedirectAttributes rAttr) {

        System.out.println("=== DELETE 요청 도착 ===");
        System.out.println("storeCode: " + storeCode + ", officeCode: " + officeCode);

        try {
            officeService.deleteOffice(officeCode);
            rAttr.addFlashAttribute("message", "사무실 삭제를 성공하였습니다.");
        } catch (Exception e) {
            rAttr.addFlashAttribute("errorMessage", "사무실 삭제를 실패하였습니다." + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/samusil/admin/officemanage/" + storeCode;

    }

    @PostMapping("/samusil/admin/officemanage/{storeCode}/{officeCode}")
    public String deleteOffice(@PathVariable("storeCode") int storeCode,
                               @PathVariable("officeCode") int officeCode) throws Exception {
        officeService.deleteOffice(officeCode);
        return "redirect:/samusil/admin/officemanage/" + storeCode;
    }

    // 사무실 수정 페이지 이동
    @GetMapping("/samusil/admin/officeedit/{officeCode}")
    public ModelAndView officeEdit(@PathVariable("officeCode") int officeCode, ModelAndView mv) {
        OfficeDTO office = officeService.findOfficeByCode(officeCode);
        List<String> officeTypes = officeService.getOfficeTypes(); // officeType 목록 가져오기
        int officePrice = office.getOfficePrice();

        mv.addObject("office", office);
        mv.addObject("officeTypes", officeTypes); // officeType 목록 전달
        mv.addObject("storeCode", office.getStoreCode()); // 추가로 storeCode 전달 가능성 확인
        mv.addObject("officePrice", officePrice);

        mv.setViewName("samusil/admin/officeedit");
        return mv; // officeedit.html로 이동
    }

    @PostMapping("/samusil/admin/officeedit/{officeCode}")
    public String adminOfficeEdit(@PathVariable("officeCode") int officeCode,
                                  @ModelAttribute OfficeDTO office,
                                  @RequestParam(value = "newImage", required = false) MultipartFile newImage,
                                  RedirectAttributes rAttr) {

        try {
            office.setOfficeCode(officeCode);

            officeService.updateOffice(office, newImage);

            rAttr.addFlashAttribute("message", "사무실 정보가 수정되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            rAttr.addFlashAttribute("message", "사무실 정보 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/samusil/admin/officeedit/" + officeCode;
        }

        return "redirect:/samusil/admin/officemanage/" + office.getStoreCode();
    }

}
