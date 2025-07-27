package com.example.isms.controller;

import com.example.isms.model.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class sendEmailToGroup {

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/send")
    public String sendEmailToGroup(@RequestBody EmailRequest emailRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailRequest.getTo());
        message.setSubject(emailRequest.getSubject());
        message.setText(emailRequest.getText());
        mailSender.send(message);
        return "Email sent successfully!";
    }
}
