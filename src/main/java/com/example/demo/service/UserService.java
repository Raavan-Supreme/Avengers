package com.example.demo.service;

import com.example.demo.dto.Resume;
import com.example.demo.dto.Skills;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.repository.SkillsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {


    @Autowired
    private SkillsRepo skillsRepo;

    @Autowired
    private ResumeRepository resumeRepo;

    public ResponseEntity<List<Resume>> getResumesBySkills(List<String> skills) {
        List<Skills> matchingSkills = skillsRepo.findByAnySkillIn(skills);
        if (matchingSkills.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Resume> resumes = resumeRepo.findBySkillsIn(matchingSkills);

        return ResponseEntity.ok(resumes);
    }
}
