package ro.uaic.asli.lab9.concurrent;

import ro.uaic.asli.lab9.model.Position;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Thread-safe shared blackboard for all robots (global exploration memory + bunny sightings).
 * Bunny positions are stored per bunny id so multiple bunnies can be tracked in advanced mode.
 */
public final class SharedMemory {

    private final Object lock = new Object();
    private final Map<Position, Integer> globalVisitCount = new HashMap<>();
    private final Map<Position, Set<Integer>> visitorsByCell = new HashMap<>();
    private final Map<Integer, Position> lastKnownBunnyById = new HashMap<>();

    public void markVisited(Position p, int robotId) {
        if (p == null || robotId < 0) {
            return;
        }
        synchronized (lock) {
            globalVisitCount.merge(p, 1, Integer::sum);
            visitorsByCell.computeIfAbsent(p, k -> new HashSet<>()).add(robotId);
        }
    }

    public boolean isVisited(Position p) {
        if (p == null) {
            return false;
        }
        synchronized (lock) {
            return globalVisitCount.getOrDefault(p, 0) > 0;
        }
    }

    /**
     * How many times other robots (not {@code robotId}) marked this cell — used to prefer less crowded cells.
     */
    public int otherRobotVisitWeight(Position p, int robotId) {
        if (p == null) {
            return 0;
        }
        synchronized (lock) {
            Set<Integer> ids = visitorsByCell.get(p);
            if (ids == null) {
                return 0;
            }
            int w = 0;
            for (Integer id : ids) {
                if (id != null && id != robotId) {
                    w++;
                }
            }
            return w;
        }
    }

    public void updateBunnyLocation(int bunnyId, Position p) {
        if (p == null || bunnyId < 0) {
            return;
        }
        synchronized (lock) {
            lastKnownBunnyById.put(bunnyId, p);
        }
    }

    public Map<Integer, Position> snapshotLastKnownBunnies() {
        synchronized (lock) {
            return Map.copyOf(lastKnownBunnyById);
        }
    }

    public void clearBunnyLocation(int bunnyId) {
        synchronized (lock) {
            lastKnownBunnyById.remove(bunnyId);
        }
    }

    public boolean hasBunnyIntel() {
        synchronized (lock) {
            return !lastKnownBunnyById.isEmpty();
        }
    }

    public Map<Position, Integer> snapshotGlobalVisits() {
        synchronized (lock) {
            return Collections.unmodifiableMap(new HashMap<>(globalVisitCount));
        }
    }
}
