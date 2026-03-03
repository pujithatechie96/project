package com.diaperbazaar.project.service;

import com.diaperbazaar.project.entity.Contact;
import com.diaperbazaar.project.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    public Contact save(Contact contact) {
        return repository.save(contact);
    }

    public List<Contact> getAll() {
        return repository.findAll();
    }
}
