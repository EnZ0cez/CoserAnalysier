package com.tourism.saas.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务类
 */
@Slf4j
@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // =============================基础操作=============================
    
    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}", key, e);
        }
    }
    
    /**
     * 设置缓存并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}, timeout: {}", key, timeout, e);
        }
    }
    
    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 获取缓存，指定类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value == null ? null : (T) value;
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败，key: {}", key, e);
        }
    }
    
    /**
     * 批量删除缓存
     */
    public void delete(Collection<String> keys) {
        try {
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("批量删除缓存失败，keys: {}", keys, e);
        }
    }
    
    /**
     * 检查缓存是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查缓存存在性失败，key: {}", key, e);
            return false;
        }
    }
    
    /**
     * 设置过期时间
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("设置过期时间失败，key: {}, timeout: {}", key, timeout, e);
        }
    }
    
    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key);
        } catch (Exception e) {
            log.error("获取过期时间失败，key: {}", key, e);
            return -1;
        }
    }
    
    // =============================Hash操作=============================
    
    /**
     * 设置Hash缓存
     */
    public void hSet(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            log.error("设置Hash缓存失败，key: {}, hashKey: {}", key, hashKey, e);
        }
    }
    
    /**
     * 获取Hash缓存
     */
    public Object hGet(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.error("获取Hash缓存失败，key: {}, hashKey: {}", key, hashKey, e);
            return null;
        }
    }
    
    /**
     * 获取所有Hash缓存
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取所有Hash缓存失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 删除Hash缓存
     */
    public void hDelete(String key, Object... hashKeys) {
        try {
            redisTemplate.opsForHash().delete(key, hashKeys);
        } catch (Exception e) {
            log.error("删除Hash缓存失败，key: {}", key, e);
        }
    }
    
    // =============================List操作=============================
    
    /**
     * 向List左侧添加元素
     */
    public void lPush(String key, Object value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("向List左侧添加元素失败，key: {}", key, e);
        }
    }
    
    /**
     * 向List右侧添加元素
     */
    public void rPush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("向List右侧添加元素失败，key: {}", key, e);
        }
    }
    
    /**
     * 从List左侧弹出元素
     */
    public Object lPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("从List左侧弹出元素失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 获取List范围内的元素
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("获取List范围内元素失败，key: {}", key, e);
            return null;
        }
    }
    
    // =============================Set操作=============================
    
    /**
     * 向Set中添加元素
     */
    public void sAdd(String key, Object... values) {
        try {
            redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("向Set中添加元素失败，key: {}", key, e);
        }
    }
    
    /**
     * 获取Set中的所有元素
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("获取Set中所有元素失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 检查Set中是否包含元素
     */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("检查Set中元素失败，key: {}", key, e);
            return false;
        }
    }
    
    // =============================ZSet操作=============================
    
    /**
     * 向ZSet中添加元素
     */
    public void zAdd(String key, Object value, double score) {
        try {
            redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error("向ZSet中添加元素失败，key: {}", key, e);
        }
    }
    
    /**
     * 获取ZSet范围内的元素（按分数升序）
     */
    public Set<Object> zRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            log.error("获取ZSet范围内元素失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 获取ZSet范围内的元素（按分数降序）
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            log.error("获取ZSet倒序范围内元素失败，key: {}", key, e);
            return null;
        }
    }
    
    // =============================分布式锁操作=============================
    
    /**
     * 尝试获取分布式锁
     */
    public boolean tryLock(String lockKey, String lockValue, long expireTime, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, unit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("获取分布式锁失败，lockKey: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 释放分布式锁
     */
    public void releaseLock(String lockKey, String lockValue) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            redisTemplate.execute(
                (connection) -> connection.eval(script.getBytes(), 
                    org.springframework.data.redis.connection.ReturnType.INTEGER, 
                    1, 
                    lockKey.getBytes(), 
                    lockValue.getBytes())
            );
        } catch (Exception e) {
            log.error("释放分布式锁失败，lockKey: {}", lockKey, e);
        }
    }
}