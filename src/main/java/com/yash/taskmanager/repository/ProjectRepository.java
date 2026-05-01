package com.yash.taskmanager.repository;

import com.yash.taskmanager.entity.Project;
import com.yash.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedBy(User user);
}

