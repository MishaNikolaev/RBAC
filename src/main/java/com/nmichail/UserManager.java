package com.nmichail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();
    private AuditLog auditLog;

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        User validated = User.validate(user.username(), user.fullName(), user.email());
        User prev = users.putIfAbsent(validated.username(), validated);
        if (prev != null) {
            throw new IllegalArgumentException("user already exists");
        }
        if (auditLog != null) {
            auditLog.log("USER_CREATE", "system", validated.username(), validated.email());
        }
    }

    @Override
    public boolean remove(User user) {
        boolean removed = users.remove(user.username(), user);
        if (removed && auditLog != null) {
            auditLog.log("USER_DELETE", "system", user.username(), "");
        }
        return removed;
    }


    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        User user = users.get(username);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
}

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.email().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }


    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = new ArrayList<>(users.values());

        if (filter != null) {
            Iterator<User> iterator = result.iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                if (!filter.test(user)) {
                    iterator.remove();
                }
            }
        }

        if (sorter != null) {
            result.sort(sorter);
        } else {
            result.sort(Comparator.comparing(User::username));
        }
        return result;
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        User validated = User.validate(username, newFullName, newEmail);
        User updated = users.computeIfPresent(username, (k, v) -> validated);
        if (updated == null) {
            throw new IllegalArgumentException("user does not exist");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}