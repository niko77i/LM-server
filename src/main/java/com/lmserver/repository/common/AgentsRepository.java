package com.lmserver.repository.common;

import com.lmserver.entity.common.Agents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentsRepository extends JpaRepository<Agents, Long> {
}