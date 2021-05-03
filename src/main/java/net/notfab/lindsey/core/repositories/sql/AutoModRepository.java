package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.profile.server.AutoModSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoModRepository extends JpaRepository<AutoModSettings, Long> {
}
