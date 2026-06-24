package com.zerobug_agent.repository;

import com.zerobug_agent.entity.Project;
import com.zerobug_agent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserOrderByCreatedAtDesc(User user);
    List<Project> findAllByOrderByCreatedAtDesc();
}
