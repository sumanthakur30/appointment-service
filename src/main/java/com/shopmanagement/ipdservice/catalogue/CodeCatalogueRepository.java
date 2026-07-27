package com.shopmanagement.ipdservice.catalogue;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodeCatalogueRepository extends JpaRepository<CodeCatalogueEntry, Long> {

    Optional<CodeCatalogueEntry> findByTenantIdAndShopIdAndSystemCodeAndCodeIgnoreCase(
            Long tenantId, String shopId, String systemCode, String code);

    @Query("""
            select e from CodeCatalogueEntry e
            where e.tenantId = :tenantId and e.shopId = :shopId
              and e.systemCode = :system and e.active = true
              and (lower(e.code) like lower(concat('%', :q, '%'))
                   or lower(e.display) like lower(concat('%', :q, '%'))
                   or lower(coalesce(e.searchText, '')) like lower(concat('%', :q, '%')))
            order by e.code
            """)
    List<CodeCatalogueEntry> search(
            @Param("tenantId") Long tenantId,
            @Param("shopId") String shopId,
            @Param("system") String system,
            @Param("q") String q);
}
