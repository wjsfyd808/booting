package org.kitchen.booting.service;

import org.kitchen.booting.controller.RecipeController;
import org.kitchen.booting.domain.Scrap;
import org.kitchen.booting.repository.ScrapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapService {
    @Autowired
    private ScrapRepository scrapRepository;

    private final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    public List<Scrap> findByUserId(String userId) {
        List<Scrap> scraps = new ArrayList<>();
        scrapRepository.findAllByUserId(userId).forEach(e->scraps.add(e));

        return scraps;
    }

    public Scrap getScrap(String userId, Long recipeNo) {
        Scrap scrap = scrapRepository.findByUserIdAndRecipeNo(userId, recipeNo);
        return scrap;
    }

    public Scrap save(Scrap scrap) {
        logger.info(String.valueOf(scrap));
        scrapRepository.save(scrap);
        return scrap;
    }

    public void delete(String userId, Long recipeNo) {
        Scrap scrap = scrapRepository.findByUserIdAndRecipeNo(userId, recipeNo);
        scrapRepository.delete(scrap);
    }

}