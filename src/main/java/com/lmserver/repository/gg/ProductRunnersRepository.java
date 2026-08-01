package com.lmserver.repository.gg;

import com.lmserver.entity.gg.ProductRunners;
import com.lmserver.entity.gg.ProductRunnersId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRunnersRepository extends JpaRepository<ProductRunners, ProductRunnersId> {
}