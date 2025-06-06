package com.example.demo.repository;

import com.example.demo.dto.SentOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SentOtpRepository extends JpaRepository<SentOtp,Integer> {
        @Query(value = "SELECT * FROM sent_otp WHERE sent_on = ?1 AND otp_type = ?2 ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
        SentOtp findTopBySentOnOrderByCreatedAtDesc(String sentOnValue,String otpType);
}
