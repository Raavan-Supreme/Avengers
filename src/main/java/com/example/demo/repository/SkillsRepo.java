package com.example.demo.repository;

import com.example.demo.dto.Resume;
import com.example.demo.dto.Skills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SkillsRepo extends JpaRepository<Skills,Integer> {
    @Query("SELECT s FROM Skills s " +
            "WHERE " +
            "EXISTS (SELECT t FROM s.technical t WHERE t IN :skills) OR " +
            "EXISTS (SELECT so FROM s.soft so WHERE so IN :skills) OR " +
            "EXISTS (SELECT tols FROM s.tools tols WHERE tols IN :skills) OR " +
            "EXISTS (SELECT l FROM s.languages l WHERE l IN :skills)")
    List<Skills> findByAnySkillIn(@Param("skills") List<String> skills);
}
