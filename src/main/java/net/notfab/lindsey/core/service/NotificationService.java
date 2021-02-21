package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.shared.entities.Notification;
import net.notfab.lindsey.shared.enums.NotificationStatus;
import net.notfab.lindsey.shared.repositories.sql.NotificationRepository;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class NotificationService {

    private final Snowflake snowflake;
    private final NotificationRepository repository;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public NotificationService(Snowflake snowflake, NotificationRepository repository) {
        this.snowflake = snowflake;
        this.repository = repository;
    }

    public void send(User user, String i18n, Object... args) {
        this.send(user.getIdLong(), i18n, args);
    }

    public void send(Member member, String i18n, Object... args) {
        this.send(member.getIdLong(), i18n, args);
    }

    public void send(Guild guild, String i18n, Object... args) {
        this.send(guild.getIdLong(), i18n, args);
    }

    public void send(Long target, String i18n, Object... args) {
        Notification notification = new Notification();
        notification.setId(this.snowflake.next());
        notification.setTarget(target);
        notification.setMessage(i18n);
        notification.setStatus(NotificationStatus.UNREAD);
        if (args != null && args.length > 0) {
            JSONArray array = new JSONArray();
            for (Object arg : args) {
                array.put(String.valueOf(arg));
            }
            notification.setParams(array);
        }
        this.repository.save(notification);
    }

    public List<Notification> getPending(Long target) {
        return repository.findAllByStatusAndTarget(NotificationStatus.UNREAD, target);
    }

    public String getTime(Notification notification) {
        long timestamp = this.snowflake.parse(notification.getId())[0];
        Date date = Date.from(Instant.ofEpochMilli(timestamp));
        return this.simpleDateFormat.format(date);
    }

}
