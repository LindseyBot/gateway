package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.profile.server.StarboardSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StarboardSettingsRepository extends JpaRepository<StarboardSettings, Long> {
}
