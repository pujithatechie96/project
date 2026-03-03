package com.diaperbazaar.project.repository;


import com.diaperbazaar.project.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
