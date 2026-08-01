package com.lmserver.repository.gg;

import com.lmserver.entity.gg.Packages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackagesRepository extends JpaRepository<Packages, Long> {
}