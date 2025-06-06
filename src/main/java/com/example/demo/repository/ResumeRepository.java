package com.example.demo.repository;

import com.example.demo.dto.Resume;
import com.example.demo.dto.Skills;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Integer> {
    List<Resume> findByName(String name);

    List<Resume> findBySkillsIn(List<Skills> matchingSkills);

    List<Resume> findByPhNo(String mobileNo);
}
