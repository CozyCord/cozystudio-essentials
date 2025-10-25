package net.cozystudios.cozystudiosessentials.util;

import net.cozystudios.cozystudiosessentials.data.PlayerData;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportUtil {

    public static boolean teleportPlayer(ServerPlayerEntity player, PlayerData.HomeData home) {
        try {
            // Get the target world
            Identifier dimensionId = new Identifier(home.getDimension());
            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimensionId);
            ServerWorld targetWorld = player.getServer().getWorld(worldKey);

            if (targetWorld == null) {
                return false;
            }

            // Save current position for /back
            PlayerData data = net.cozystudios.cozystudiosessentials.CozyStudiosEssentials
                    .getPlayerDataManager().getPlayerData(player);
            data.setLastPosition(
                    player.getPos(),
                    player.getWorld().getRegistryKey().getValue().toString()
            );

            // Teleport the player
            player.teleport(
                    targetWorld,
                    home.getX(),
                    home.getY(),
                    home.getZ(),
                    home.getYaw(),
                    home.getPitch()
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean teleportPlayer(ServerPlayerEntity player, ServerWorld world,
                                         double x, double y, double z, float yaw, float pitch) {
        try {
            // Save current position for /back
            PlayerData data = net.cozystudios.cozystudiosessentials.CozyStudiosEssentials
                    .getPlayerDataManager().getPlayerData(player);
            data.setLastPosition(
                    player.getPos(),
                    player.getWorld().getRegistryKey().getValue().toString()
            );

            // Teleport
            player.teleport(world, x, y, z, yaw, pitch);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Find a safe location near the target position
    public static BlockPos findSafeLocation(ServerWorld world, BlockPos target) {
        // Simple implementation - can be enhanced later
        for (int y = target.getY(); y < world.getTopY(); y++) {
            BlockPos pos = new BlockPos(target.getX(), y, target.getZ());
            if (world.getBlockState(pos).isAir() &&
                    world.getBlockState(pos.up()).isAir() &&
                    !world.getBlockState(pos.down()).isAir()) {
                return pos;
            }
        }
        return target;
    }
}