package net.notfab.lindsey.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.security.auth.login.LoginException;

@SpringBootApplication
public class Application implements ApplicationRunner {

    @Value("${bot.token}")
    private String token;

    @Autowired
    private Lindsey lindsey;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        lindsey.init();
    }

    @Bean
    public ShardManager shardManager() throws LoginException {
        return DefaultShardManagerBuilder.createDefault(this.token)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .disableCache(CacheFlag.CLIENT_STATUS, CacheFlag.ACTIVITY)
            .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
