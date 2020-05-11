package net.notfab.lindsey.framework.settings.repositories;

import net.notfab.lindsey.framework.settings.UserSettings;
import org.springframework.data.repository.CrudRepository;

public interface UserSettingsRepository extends CrudRepository<UserSettings, Long> {

}
