package com.hotel.reception.repository;

import com.hotel.reception.model.entity.ExtraService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtraServiceRepository extends JpaRepository<ExtraService, Long> {
    
    Optional<ExtraService> findByServiceCode(String serviceCode);
    
    List<ExtraService> findByIsActiveTrue();
    
    List<ExtraService> findByCategory(String category);
    
//    List<ExtraService> findByCategoryAndIsActiveTrue(String category);

    @Query("SELECT e FROM ExtraService e WHERE e.category = :category AND e.isActive = :isActive")
    List<ExtraService> findActiveByCategory(@Param("category") String category, 
                                            @Param("isActive") Boolean isActive);
}
