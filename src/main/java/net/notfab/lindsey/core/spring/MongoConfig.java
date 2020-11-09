package net.notfab.lindsey.core.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "net.notfab.lindsey")
public class MongoConfig {
}
