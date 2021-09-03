package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.server.WelcomeSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WelcomeSettingsRepository extends JpaRepository<WelcomeSettings, Long> {
}
