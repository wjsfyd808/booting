package org.kitchen.booting.controller;

import jdk.nashorn.internal.objects.annotations.Getter;
import org.kitchen.booting.domain.Profile;
import org.kitchen.booting.service.LikeService;
import org.kitchen.booting.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class KitchenController {

    @Autowired
    ProfileService profileService;
    @Autowired
    LikeService likeService;

    @GetMapping(value = "/profile/register")
    public void profileForm(@ModelAttribute Profile profile) {
        //return "profile/register";
    }

    @PostMapping(value = "/profile/register")
    public void saveProfile(@ModelAttribute Profile profile) {
        profileService.save(profile);
       // return "profile/register";
    }

    @GetMapping(value = "/profile/list")
    public void profileList(Model model) {
        model.addAttribute("profiles", profileService.findAll());
    }

    @GetMapping(value = "/profile/likelist")
    public void likeList(Model model) {
        // 세션에서 유저의 아이디를 받아서 리스트로 보내준다
        model.addAttribute("likeList", likeService.listByUserId("user01"));
    }

}
