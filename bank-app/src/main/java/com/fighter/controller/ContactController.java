package com.fighter.controller;

import com.fighter.model.Contact;
import com.fighter.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;

    @PostMapping("/contact")
   // @PreFilter("filterObject.contactName != 'Test'")
    @PostFilter("filterObject.contactName != 'Test'")
    public List<Contact> saveContactInquiryDetails(@RequestBody List<Contact> contact) {

        List<Contact> returnedContact = new ArrayList<>();
        if (!contact.isEmpty()) {
            Contact con = contact.getFirst();
            con.setContactId(this.getServiceReqNumber());
            con.setCreateDt(new Date(System.currentTimeMillis()));
            Contact savedContact = contactRepository.save(con);
            returnedContact.add(savedContact);
        }
        return returnedContact;
    }

    public String getServiceReqNumber() {
        Random random = new Random();
        int ranNum = random.nextInt(999999999 - 9999) + 9999;
        return "SR" + ranNum;
    }
}