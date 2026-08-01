package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbProductsRepository extends JpaRepository<FbProducts, Long> {
}