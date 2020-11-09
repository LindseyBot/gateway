package net.notfab.lindsey.core.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "net.notfab.lindsey.core.repositories.mongo")
public class MongoConfig {
}
