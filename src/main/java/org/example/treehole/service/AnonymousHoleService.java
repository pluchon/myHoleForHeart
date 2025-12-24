package org.example.treehole.service;

import org.example.treehole.entry.AnonymousHole;
import org.example.treehole.mapper.AnonymousHoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnonymousHoleService {
    @Autowired
    private AnonymousHoleMapper anonymousHoleMapper;

    public AnonymousHole getRandomHole() {
        return anonymousHoleMapper.getRandomHole();
    }

    public boolean add(AnonymousHole hole) {
        if (hole == null || hole.getContent() == null || hole.getContent().trim().isEmpty()) {
            return false;
        }
        anonymousHoleMapper.insert(hole);
        return true;
    }
}
