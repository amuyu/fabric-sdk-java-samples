package com.github.amuyu.fabric;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hyperledger.fabric.sdk.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Hyperledger blockchain 채널 관리
 */
public class ChannelManger implements RemovalListener<String, Channel> {
    private static Logger logger = LoggerFactory.getLogger(ChannelManger.class);

    static Long defaultDuration = 1L;
    static TimeUnit defaultTimeUnit = TimeUnit.HOURS;

    Cache<String, Channel> cache;

    public ChannelManger() {
        this(defaultDuration, defaultTimeUnit, null);
    }

    public ChannelManger(RemovalListener<String, Channel> removalListener) {
        this(defaultDuration, defaultTimeUnit, removalListener);
    }

    public ChannelManger(Long duration, TimeUnit unit) {
        this(duration, unit, null);
    }

    public ChannelManger(Long duration, TimeUnit unit, RemovalListener<String, Channel> removalListener) {
        Caffeine caffeine = Caffeine.newBuilder()
                .expireAfterWrite(duration, unit)
                .maximumSize(10);
        if (removalListener != null) {
            caffeine.removalListener(removalListener);
        } else {
            caffeine.removalListener(this);
        }
        cache = caffeine.build();
    }

    public void put(Channel channel) {
        cache.put(channel.getName(), channel);
    }

    public Channel get(String channelName) {
        return cache.getIfPresent(channelName);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public void invalidate(String channelName) {
        Channel c = get(channelName);
        if (c != null) {
            cache.invalidate(c.getName());
        }
    }

    @Override
    public void onRemoval(@Nullable String key, @Nullable Channel value, @NonNull RemovalCause cause) {
        logger.debug("onRemoval key:{}, channel:{}, cause:{}", key, value ,cause);
        Channel c = get(key);
        if (c == null && value != null) {
            logger.debug("channel:{} is shutdown", value.getName());
            value.shutdown(true);
        }
    }
}
