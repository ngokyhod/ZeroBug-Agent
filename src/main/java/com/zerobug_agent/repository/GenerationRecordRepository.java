package com.zerobug_agent.repository;

import com.zerobug_agent.entity.GenerationRecord;
import com.zerobug_agent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenerationRecordRepository extends JpaRepository<GenerationRecord, Long> {
    List<GenerationRecord> findByUserOrderByCreatedAtDesc(User user);
}
