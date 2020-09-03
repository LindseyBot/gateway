package net.notfab.lindsey.core.spring;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "net.notfab.lindsey")
@EnableJpaRepositories(basePackages = "net.notfab.lindsey")
public class MariaConfig {
}
