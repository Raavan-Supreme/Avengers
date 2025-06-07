package com.example.demo.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
        name = "user",
        indexes = {
                @Index(name = "unique_email_index", columnList = "email", unique = true)
        }
)

public class User implements Serializable{

    @Id
    @Column(name = "uuid")
    private String Uuid;

    @Column
    private String name;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;

    @Column(name = "password")
    private String password;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @Column
    @Enumerated(EnumType.STRING)
    private UserGender gender;

    public enum UserGender {
        MALE, FEMALE, CHOOSE_NOT_TO_DISCLOSE
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, REGISTRATION_PENDING, DELETED
    }

    public enum UserType {
        USER, DOCTOR, SUPER_ADMIN, SUPPORT_ADMIN
    }

}
