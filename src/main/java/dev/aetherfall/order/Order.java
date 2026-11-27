package dev.aetherfall.order;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A player Order — the new wardens of the Vale. Stored in data/orders.yml. */
public final class Order {
    private final String name;
    private final UUID leader;
    private final List<UUID> members = new ArrayList<>();
    private String homeWorld;
    private int homeX;
    private int homeY;
    private int homeZ;

    public Order(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
    }

    public String getName() {
        return name;
    }

    public UUID getLeader() {
        return leader;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public String getHomeWorld() {
        return homeWorld;
    }

    public void setHome(String world, int x, int y, int z) {
        this.homeWorld = world;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
    }

    public int getHomeX() {
        return homeX;
    }

    public int getHomeY() {
        return homeY;
    }

    public int getHomeZ() {
        return homeZ;
    }

    public boolean hasHome() {
        return homeWorld != null;
    }
}
