package org.example.utils;

import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CacheUtil {

    private static final String CACHE_MAP_NAME = "githubCache";
    private final RedissonClient redissonClient;
    private final RMapCache<String, List<String>> cacheMap;

    public CacheUtil() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        this.redissonClient = Redisson.create(config);
        this.cacheMap = redissonClient.getMapCache(CACHE_MAP_NAME);
    }

    private String getKey(String owner, String repo, String branch) {
        return owner + "_" + repo + "_" + branch;
    }

    public void put(String owner, String repo, String branch, List<String> value) {
        String key = getKey(owner, repo, branch);
        cacheMap.put(key, value, 10, TimeUnit.MINUTES);
    }

    public List<String> get(String owner, String repo, String branch) {
        return cacheMap.getOrDefault(getKey(owner, repo, branch), new ArrayList<>());
    }

    public void close() {
        redissonClient.shutdown();
    }
}
