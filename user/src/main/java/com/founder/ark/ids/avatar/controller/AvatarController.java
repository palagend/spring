package com.founder.ark.ids.avatar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AvatarController {

    @GetMapping("/admin")
    public String admin() {
        return "This is admin endpoint.";
    }

    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "You have been redirected to ACCESS_DENIED page !";
    }
}
