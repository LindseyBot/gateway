package net.notfab.lindsey.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.notfab.eventti.Event;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class RedisQueueService {

    private final JedisPool pool;
    private final EventService eventService;
    private final ObjectMapper objectMapper;

    private final AtomicBoolean flag = new AtomicBoolean(true);
    private final Set<String> queues;

    public RedisQueueService(JedisPool pool, EventService eventService, ObjectMapper objectMapper) {
        this.pool = pool;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
        this.queues = new HashSet<>();
    }

    /**
     * Subscribes to a new queue.
     *
     * @param queueName Name of the queue.
     */
    public void subscribe(String queueName) {
        if (this.queues.isEmpty()) {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(this::loopRedis);
            service.shutdown();
        }
        this.queues.add(queueName);
    }

    /**
     * Adds a message to processing.
     *
     * @param queueName Name of the queue.
     * @param event     Event to publish.
     */
    public void publish(String queueName, Event event) {
        try (Jedis jedis = this.pool.getResource()) {
            String data = this.objectMapper.writeValueAsString(event);
            jedis.rpush(queueName, data);
        } catch (JedisException | JsonProcessingException ex) {
            log.error("Failed to publish event to queue", ex);
        }
    }

    /**
     * Shuts down the redis queueing service.
     */
    public void shutdown() {
        this.flag.set(false);
        this.queues.clear();
    }

    private void loopRedis() {
        try (Jedis redis = this.pool.getResource()) {
            do {
                if (this.queues.isEmpty()) {
                    continue;
                }
                List<String> list = redis.blpop(5, this.queues.toArray(new String[0]));
                if (list == null || list.isEmpty()) {
                    continue;
                }
                for (String data : list) {
                    if (data == null || data.isBlank()) {
                        continue;
                    }
                    this.eventService.onMessage(data);
                }
            } while (flag.get());
        } catch (JedisException ex) {
            log.error("Failed to connect to redis", ex);
        }
    }

}
