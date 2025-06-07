package com.example.demo.repository;

import com.example.demo.dto.CandidateMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateMatchRepository extends JpaRepository<CandidateMatch, Long> {
}

