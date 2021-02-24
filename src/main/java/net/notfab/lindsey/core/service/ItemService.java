package net.notfab.lindsey.core.service;

import net.notfab.lindsey.shared.entities.items.Background;
import net.notfab.lindsey.shared.entities.items.Badge;
import net.notfab.lindsey.shared.repositories.sql.items.BackgroundRepository;
import net.notfab.lindsey.shared.repositories.sql.items.BadgeRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final BackgroundRepository backgroundRepository;
    private final BadgeRepository badgeRepository;

    public ItemService(BackgroundRepository backgroundRepository, BadgeRepository badgeRepository) {
        this.backgroundRepository = backgroundRepository;
        this.badgeRepository = badgeRepository;
    }

    public Background getBackground(long id) {
        return this.backgroundRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Unknown background"));
    }

    public Badge getBadge(long id) {
        return this.badgeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Unknown badge"));
    }

}
