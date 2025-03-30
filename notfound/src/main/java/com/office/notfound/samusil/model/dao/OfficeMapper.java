package com.office.notfound.samusil.model.dao;

import com.office.notfound.samusil.model.dto.OfficeDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OfficeMapper {

    List<OfficeDTO> findAllOffices(int storeCode);

    OfficeDTO findOfficeDetail(int officeCode);

    OfficeDTO findOfficeByStore(int storeCode, int officeCode);

    void updateOffice(OfficeDTO office);

    void deleteStore(int officeCode);

    void insertOffice(OfficeDTO office);

    //    @Delete("DELETE FROM tbl_office WHERE store_code = #{storeCode} AND office_code = #{officeCode}")
//    int deleteOfficeByStoreAndOfficeCode(@Param("storeCode") int storeCode, @Param("officeCode") int officeCode);

    int deleteOffice(int officeCode);

    List<String> getOfficeTypes();

    OfficeDTO findOfficeByCode(int officeCode);

    void deleteOfficeImageUrl(int officeCode);

    void deleteOfficeRelated(int officeCode);
}
