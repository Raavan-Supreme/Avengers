package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class Response {
    private String email;
    private String name;
    private String phNo;
    private List<Resume> response;
}
