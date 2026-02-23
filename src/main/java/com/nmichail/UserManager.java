package com.nmichail;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        if (users.containsKey(user.username())) {
            throw new IllegalArgumentException("user already exists");
        }
        User validated = User.validate(user.username(), user.fullName(), user.email());
        users.put(validated.username(), validated);
    }

    @Override
    public boolean remove(User user) {
        return users.remove(user.username(), user);
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
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("user does not exist");
        }
        User validated = User.validate(username, newFullName, newEmail);
        users.put(username, validated);
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