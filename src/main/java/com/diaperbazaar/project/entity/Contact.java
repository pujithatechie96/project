package com.diaperbazaar.project.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_us")
@Data
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String mobile;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String query;

    private LocalDateTime createdAt = LocalDateTime.now();
}
