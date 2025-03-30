package com.office.notfound.store.model.dao;

import com.office.notfound.store.model.dto.StoreDTO;
import org.apache.catalina.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Mapper
public interface StoreMapper {

    List<StoreDTO> findAllStores();

    StoreDTO findStoreByCode(int storeCode);

    List<String> findDistinctCities();

    List<String> findGuByCity(String city);

    void insertStore(StoreDTO store);

    List<StoreDTO> findStoresByCityAndGu(@Param("city") String city, @Param("gu") String gu);

    void updateStore(StoreDTO store);

    void deleteStore(int storeCode);

    String findImageByStoreCode(int storeCode);

    void deleteStoreImageUrl(int storeCode);

    StoreDTO findStoreByName(String storeName);
}