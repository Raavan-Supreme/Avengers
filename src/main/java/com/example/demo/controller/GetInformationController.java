package com.example.demo.controller;

import com.example.demo.dto.Resume;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GetInformationController {

    private final ResumeRepository resumeRepository;

    @GetMapping
    public ResponseEntity<List<Resume>> getResume(@RequestBody(required = false)
                                                      String name,String mobileNo) {
        try {
            if (name != null)
                return ResponseEntity.ok().body(resumeRepository.findByName(name));
            if (mobileNo != null)
                return ResponseEntity.ok().body(resumeRepository.findByPhNo(mobileNo));
            return ResponseEntity.ok().body(resumeRepository.findAll());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private UserService userService;

    @GetMapping("/skills")
    public ResponseEntity<List<Resume>> getResumeSkills(@RequestBody(required = false)
                                                  List<String> skills) {
        try {
            if (skills.isEmpty())
                return ResponseEntity.ok().body(resumeRepository.findAll());
            else
                return userService.getResumesBySkills(skills);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
