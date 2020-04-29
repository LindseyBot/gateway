package net.notfab.lindsey.core;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.security.auth.login.LoginException;

@SpringBootApplication(scanBasePackages = {"net.notfab.lindsey.core", "net.notfab.lindsey.framework"})
public class Application implements ApplicationRunner {

    @Value("${bot.token}")
    private String token;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        lindsey().init();
    }

    public Lindsey lindsey() throws LoginException {
        return new Lindsey(shardManager());
    }

    @Bean
    public ShardManager shardManager() throws LoginException {
        return DefaultShardManagerBuilder.createDefault(this.token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .disableCache(CacheFlag.CLIENT_STATUS, CacheFlag.ACTIVITY).build();
    }

}
