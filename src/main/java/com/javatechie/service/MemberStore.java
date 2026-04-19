/*
package com.javatechie.service;

import com.javatechie.entity.Message;
import com.javatechie.entity.WsUser;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MemberStore {

    // Key: user email, Value: WsUser
    private final Map<String, WsUser> members = new ConcurrentHashMap<>();

    // Store offline/unread messages
    private final Map<String, List<Message>> offlineMessages = new ConcurrentHashMap<>();

    // Store all registered users (for admin view)
    private final Map<String, WsUser> allUsers = new ConcurrentHashMap<>();

    // Add or update a member
    public void addMember(WsUser user) {
        if (user != null && user.email() != null) {
            members.put(user.email(), user);
            allUsers.put(user.email(), user); // Ensure all registered users are stored
        }
    }

    public void removeMember(WsUser user) {
        if (user != null && user.email() != null) {
            members.remove(user.email());
        }
    }

    public void removeMember(String email) {
        if (email != null) {
            members.remove(email);
        }
    }

    public WsUser getMember(String email) {
        return email != null ? members.get(email) : null;
    }

    public boolean isOnline(String email) {
        return email != null && members.containsKey(email);
    }

    public Map<String, WsUser> getMemberList() {
        return members;
    }

    // Return all registered users for admin
    public Map<String, WsUser> getAllUsers() {
        return allUsers;
    }

    // ---- Offline Messages ----
    public void storeOfflineMessage(String email, Message msg) {
        if (email != null && msg != null) {
            offlineMessages.computeIfAbsent(email, k -> new ArrayList<>()).add(msg);
        }
    }

    public List<Message> getOfflineMessages(String email) {
        return email != null ? offlineMessages.getOrDefault(email, Collections.emptyList()) : Collections.emptyList();
    }

    public void clearOfflineMessages(String email) {
        if (email != null) {
            offlineMessages.remove(email);
        }
    }
}

 */

package com.javatechie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.entity.Message;
import com.javatechie.entity.WsUser;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MemberStore {

    private static final String ONLINE_USERS_KEY = "online:users";
    private static final String USER_KEY_PREFIX = "users:";
    private static final String MESSAGE_STREAM_PREFIX = "messages:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MemberStore(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // ------------------ Online/Offline Tracking ------------------

    public void addMember(WsUser user) {
        if (user != null && user.email() != null) {
            String email = user.email();
            try {
                // Create new user object with isActive = true
                WsUser activeUser = new WsUser(user.id(), user.username(), user.email(), true);

                // Add to online set
                redisTemplate.opsForSet().add(ONLINE_USERS_KEY, email);

                // Store in registered users
                String userJson = objectMapper.writeValueAsString(activeUser);
                redisTemplate.opsForValue().set(USER_KEY_PREFIX + email, userJson);

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing WsUser", e);
            }
        }
    }

    public void removeMember(WsUser user) {
        if (user != null && user.email() != null) {
            updateUserStatus(user.email(), false); // mark inactive
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, user.email());
        }
    }

    public void removeMember(String email) {
        if (email != null) {
            updateUserStatus(email, false); // mark inactive
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, email);
        }
    }

    public boolean isOnline(String email) {
        if (email == null) return false;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, email));
    }

    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }

    public List<WsUser> getAllUsers() {
        Set<String> keys = Objects.requireNonNull(redisTemplate.keys(USER_KEY_PREFIX + "*"));
        return keys.stream().map(k -> {
            String json = redisTemplate.opsForValue().get(k);
            try {
                return objectMapper.readValue(json, WsUser.class);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ------------------ Offline Messages (Streams) ------------------

    public void storeOfflineMessage(String email, Message msg) {
        if (email != null && msg != null) {
            try {
                String msgJson = objectMapper.writeValueAsString(msg);
                StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
                streamOps.add(ObjectRecord.create(MESSAGE_STREAM_PREFIX + email, msgJson));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing Message", e);
            }
        }
    }

    public List<Message> getOfflineMessages(String email) {
        if (email == null) return Collections.emptyList();

        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
        List<ObjectRecord<String, String>> records = streamOps.read(
                String.class,
                org.springframework.data.redis.connection.stream.StreamOffset.fromStart(MESSAGE_STREAM_PREFIX + email)
        );

        if (records == null) return Collections.emptyList();

        List<Message> messages = new ArrayList<>();
        for (ObjectRecord<String, String> record : records) {
            try {
                messages.add(objectMapper.readValue(record.getValue(), Message.class));
            } catch (Exception e) {
                // Skip bad data
            }
        }
        return messages;
    }

    public void clearOfflineMessages(String email) {
        if (email != null) {
            redisTemplate.delete(MESSAGE_STREAM_PREFIX + email);
        }
    }


    // Helper to update user status in Redis
    private void updateUserStatus(String email, boolean active) {
        String json = redisTemplate.opsForValue().get(USER_KEY_PREFIX + email);
        if (json != null) {
            try {
                WsUser existingUser = objectMapper.readValue(json, WsUser.class);
                WsUser updatedUser = new WsUser(existingUser.id(), existingUser.username(), existingUser.email(), active);
                redisTemplate.opsForValue().set(USER_KEY_PREFIX + email, objectMapper.writeValueAsString(updatedUser));
            } catch (Exception e) {
                throw new RuntimeException("Error updating WsUser status", e);
            }
        }
    }
}

