package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.entity.Contact;
import com.diaperbazaar.project.service.ContactService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping("/contact")
    public Contact saveContact(@RequestBody Contact contact) {
        return service.save(contact);
    }

    @GetMapping("/contacts")
    public List<Contact> getAllContacts() {
        return service.getAll();
    }
}
