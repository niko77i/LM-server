package com.lmserver.repository.gg;

import com.lmserver.entity.gg.ProductAssets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAssetsRepository extends JpaRepository<ProductAssets, Long> {
}