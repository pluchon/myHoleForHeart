package org.example.treehole.controller;

import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.Hole;
import org.example.treehole.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/toggle")
    public boolean toggle(Long holeId, HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (userId == null) return false;
        return favoriteService.toggleFavorite(userId, holeId);
    }

    @RequestMapping("/my")
    public List<Hole> myFavorites(HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (userId == null) return new ArrayList<>();
        return favoriteService.getMyFavorites(userId);
    }
}
