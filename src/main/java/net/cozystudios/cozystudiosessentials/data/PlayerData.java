package net.cozystudios.cozystudiosessentials.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID playerUuid;
    private final Map<String, HomeData> homes;
    private Vec3d lastPosition;
    private String lastDimension;
    private int maxHomes = 5; // Default max homes

    public PlayerData(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.homes = new HashMap<>();
    }

    // Home management
    public boolean addHome(String name, ServerPlayerEntity player) {
        if (homes.size() >= maxHomes) {
            return false;
        }

        HomeData home = new HomeData(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.getWorld().getRegistryKey().getValue().toString()
        );

        homes.put(name.toLowerCase(), home);
        return true;
    }

    public boolean removeHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public HomeData getHome(String name) {
        return homes.get(name.toLowerCase());
    }

    public Map<String, HomeData> getHomes() {
        return homes;
    }

    public int getHomeCount() {
        return homes.size();
    }

    public int getMaxHomes() {
        return maxHomes;
    }

    public void setMaxHomes(int max) {
        this.maxHomes = max;
    }

    // Last position (for /back)
    public void setLastPosition(Vec3d pos, String dimension) {
        this.lastPosition = pos;
        this.lastDimension = dimension;
    }

    public Vec3d getLastPosition() {
        return lastPosition;
    }

    public String getLastDimension() {
        return lastDimension;
    }

    // NBT Serialization
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("UUID", playerUuid);
        nbt.putInt("MaxHomes", maxHomes);

        // Save homes
        NbtList homesList = new NbtList();
        for (Map.Entry<String, HomeData> entry : homes.entrySet()) {
            NbtCompound homeNbt = new NbtCompound();
            homeNbt.putString("Name", entry.getKey());
            homeNbt.put("Data", entry.getValue().toNbt());
            homesList.add(homeNbt);
        }
        nbt.put("Homes", homesList);

        // Save last position
        if (lastPosition != null) {
            NbtCompound lastPos = new NbtCompound();
            lastPos.putDouble("X", lastPosition.x);
            lastPos.putDouble("Y", lastPosition.y);
            lastPos.putDouble("Z", lastPosition.z);
            lastPos.putString("Dimension", lastDimension);
            nbt.put("LastPosition", lastPos);
        }

        return nbt;
    }

    public static PlayerData fromNbt(NbtCompound nbt) {
        UUID uuid = nbt.getUuid("UUID");
        PlayerData data = new PlayerData(uuid);

        data.maxHomes = nbt.getInt("MaxHomes");

        // Load homes
        NbtList homesList = nbt.getList("Homes", 10); // 10 = Compound type
        for (int i = 0; i < homesList.size(); i++) {
            NbtCompound homeNbt = homesList.getCompound(i);
            String name = homeNbt.getString("Name");
            HomeData home = HomeData.fromNbt(homeNbt.getCompound("Data"));
            data.homes.put(name, home);
        }

        // Load last position
        if (nbt.contains("LastPosition")) {
            NbtCompound lastPos = nbt.getCompound("LastPosition");
            data.lastPosition = new Vec3d(
                    lastPos.getDouble("X"),
                    lastPos.getDouble("Y"),
                    lastPos.getDouble("Z")
            );
            data.lastDimension = lastPos.getString("Dimension");
        }

        return data;
    }

    // Inner class for home data
    public static class HomeData {
        private final double x, y, z;
        private final float yaw, pitch;
        private final String dimension;

        public HomeData(double x, double y, double z, float yaw, float pitch, String dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.dimension = dimension;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public float getYaw() { return yaw; }
        public float getPitch() { return pitch; }
        public String getDimension() { return dimension; }

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putDouble("X", x);
            nbt.putDouble("Y", y);
            nbt.putDouble("Z", z);
            nbt.putFloat("Yaw", yaw);
            nbt.putFloat("Pitch", pitch);
            nbt.putString("Dimension", dimension);
            return nbt;
        }

        public static HomeData fromNbt(NbtCompound nbt) {
            return new HomeData(
                    nbt.getDouble("X"),
                    nbt.getDouble("Y"),
                    nbt.getDouble("Z"),
                    nbt.getFloat("Yaw"),
                    nbt.getFloat("Pitch"),
                    nbt.getString("Dimension")
            );
        }
    }
}