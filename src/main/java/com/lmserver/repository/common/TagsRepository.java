package com.lmserver.repository.common;

import com.lmserver.entity.common.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagsRepository extends JpaRepository<Tags, String> {
}