package org.example.treehole.service;

import org.example.treehole.entry.Favorite;
import org.example.treehole.entry.Hole;
import org.example.treehole.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    public boolean toggleFavorite(Long userId, Long holeId) {
        if (favoriteMapper.count(userId, holeId) > 0) {
            favoriteMapper.delete(userId, holeId);
            return false; // Unfavorited
        } else {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setHoleId(holeId);
            favoriteMapper.insert(f);
            return true; // Favorited
        }
    }

    public List<Hole> getMyFavorites(Long userId) {
        return favoriteMapper.selectByUserId(userId);
    }
}
