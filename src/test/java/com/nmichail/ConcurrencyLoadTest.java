package com.nmichail;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.fail;

public class ConcurrencyLoadTest {

    @Test
    void parallelMutationsAndQueries_EXPECT_noUnexpectedExceptions() throws Exception {
        RBACSystem system = new RBACSystem();
        system.initialize();

        int workers = Math.max(4, Runtime.getRuntime().availableProcessors());
        int iterationsPerWorker = 400;

        ExecutorService pool = Executors.newFixedThreadPool(workers);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(workers);

        List<Throwable> errors = new java.util.concurrent.CopyOnWriteArrayList<>();
        AtomicInteger userSeq = new AtomicInteger(1);
        AtomicInteger roleSeq = new AtomicInteger(1);

        for (int w = 0; w < workers; w++) {
            final int workerId = w;
            pool.submit(() -> {
                Random rnd = new Random(12345L + workerId);
                try {
                    start.await();
                    for (int i = 0; i < iterationsPerWorker; i++) {
                        int op = rnd.nextInt(8);
                        try {
                            switch (op) {
                                case 0 -> createUser(system, userSeq);
                                case 1 -> updateRandomUser(system, rnd);
                                case 2 -> createRole(system, roleSeq);
                                case 3 -> assignRandomRole(system, rnd);
                                case 4 -> queryUsers(system, rnd);
                                case 5 -> queryRoles(system, rnd);
                                case 6 -> queryAssignments(system, rnd);
                                case 7 -> permissionMatrixWarmup(system, rnd);
                                default -> {
                                }
                            }
                        } catch (IllegalArgumentException | IllegalStateException ignored) {
                        } catch (Throwable t) {
                            errors.add(t);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        if (!done.await(20, TimeUnit.SECONDS)) {
            fail("Load test timed out");
        }
        pool.shutdownNow();

        system.getAuditLog().flush(1000);

        if (!errors.isEmpty()) {
            Throwable first = errors.get(0);
            first.printStackTrace();
            fail("Unexpected exception in concurrent load: " + first);
        }
    }

    private static void createUser(RBACSystem system, AtomicInteger userSeq) {
        String username = "u" + userSeq.getAndIncrement();
        User u = User.validate(username, "User " + username, username + "@example.com");
        system.getUserManager().add(u);
    }

    private static void updateRandomUser(RBACSystem system, Random rnd) {
        List<User> all = system.getUserManager().findAll();
        if (all.isEmpty()) return;
        User u = all.get(rnd.nextInt(all.size()));
        system.getUserManager().update(u.username(), u.fullName() + " Jr", u.username() + "@example.com");
    }

    private static void createRole(RBACSystem system, AtomicInteger roleSeq) {
        String name = "R" + roleSeq.getAndIncrement();
        Set<Permission> perms = Set.of(
                new Permission("READ", "res-" + (roleSeq.get() % 5), "read"),
                new Permission("WRITE", "res-" + (roleSeq.get() % 5), "write")
        );
        system.getRoleManager().add(new Role(name, "desc " + name, perms));
    }

    private static void assignRandomRole(RBACSystem system, Random rnd) {
        List<User> users = system.getUserManager().findAll();
        List<Role> roles = system.getRoleManager().findAll();
        if (users.isEmpty() || roles.isEmpty()) return;
        User u = users.get(rnd.nextInt(users.size()));
        Role r = roles.get(rnd.nextInt(roles.size()));
        AssignmentMetadata meta = AssignmentMetadata.now("system", "load test");
        RoleAssignment a = new PermanentAssignment(u, r, meta);
        system.getAssignmentManager().add(a);
    }

    private static void queryUsers(RBACSystem system, Random rnd) {
        String needle = "u" + (1 + rnd.nextInt(50));
        system.getUserManager().findByFilterParallel(UserFilters.byUsernameContains(needle));
        system.getUserManager().findByUsername(needle);
    }

    private static void queryRoles(RBACSystem system, Random rnd) {
        String needle = "R" + (1 + rnd.nextInt(50));
        system.getRoleManager().findByFilterParallel(RoleFilters.byNameContains(needle));
        system.getRoleManager().findByName(needle);
    }

    private static void queryAssignments(RBACSystem system, Random rnd) {
        List<RoleAssignment> all = system.getAssignmentManager().findAll();
        if (all.isEmpty()) return;
        RoleAssignment a = all.get(rnd.nextInt(all.size()));
        system.getAssignmentManager().findById(a.assignmentId());
        system.getAssignmentManager().findByFilterParallel(AssignmentFilters.byType(a.assignmentType()));
    }

    private static void permissionMatrixWarmup(RBACSystem system, Random rnd) {
        List<User> users = system.getUserManager().findAll();
        if (users.isEmpty()) return;
        User u = users.get(rnd.nextInt(users.size()));
        system.getAssignmentManager().getUserPermissions(u);
        system.getAuditLog().log("LOAD_TEST", "worker", UUID.randomUUID().toString(), "ping");
    }
}

