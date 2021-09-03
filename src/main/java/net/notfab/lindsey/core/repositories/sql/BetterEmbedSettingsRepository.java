package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.profile.server.BetterEmbedsSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetterEmbedSettingsRepository extends JpaRepository<BetterEmbedsSettings, Long> {
}
