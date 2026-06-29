package com.skrra.atmosphereplus.environment;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class EnvironmentDetector {
    private static final int UPDATE_INTERVAL_TICKS = 10;
    private static int ticksUntilUpdate = 0;
    private static EnvironmentSnapshot current = EnvironmentSnapshot.unknown();

    private EnvironmentDetector() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(EnvironmentDetector::tick);
    }

    public static EnvironmentSnapshot current() {
        return current;
    }

    public static boolean isSurface() {
        return current.surface();
    }

    public static boolean isUnderground() {
        return current.underground();
    }

    public static boolean isCaveLike() {
        return current.caveLike();
    }

    public static boolean canSeeSky() {
        return current.canSeeSky();
    }

    public static boolean isInNether() {
        return current.nether();
    }

    public static boolean isInEnd() {
        return current.end();
    }

    private static void tick(MinecraftClient client) {
        if (--ticksUntilUpdate > 0) {
            return;
        }

        ticksUntilUpdate = UPDATE_INTERVAL_TICKS;
        current = detect(client);
    }

    private static EnvironmentSnapshot detect(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return EnvironmentSnapshot.unknown();
        }

        if (World.NETHER.equals(client.world.getRegistryKey())) {
            return new EnvironmentSnapshot(EnvironmentType.NETHER, false, false, false, false, true, false);
        }
        if (World.END.equals(client.world.getRegistryKey())) {
            return new EnvironmentSnapshot(EnvironmentType.END, true, true, false, false, false, true);
        }

        BlockPos pos = client.player.getBlockPos();
        boolean canSeeSky = client.world.isSkyVisible(pos);
        int seaLevel = client.world.getSeaLevel();
        int y = pos.getY();

        Overhead overhead = scanOverhead(client, pos);
        boolean deepBelowSurface = y < seaLevel - 10;
        boolean veryDeep = y < seaLevel - 24;
        boolean enclosed = !canSeeSky && overhead.solidBlocks >= 3;
        boolean caveLike = !canSeeSky && (veryDeep || (deepBelowSurface && overhead.firstSolidDistance <= 12) || overhead.solidBlocks >= 8);
        boolean underground = !canSeeSky && (deepBelowSurface || enclosed || caveLike);
        boolean surface = !underground;

        EnvironmentType type = caveLike ? EnvironmentType.CAVE : underground ? EnvironmentType.UNDERGROUND : EnvironmentType.SURFACE;
        return new EnvironmentSnapshot(type, canSeeSky, surface, underground, caveLike, false, false);
    }

    private static Overhead scanOverhead(MinecraftClient client, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int solidBlocks = 0;
        int firstSolidDistance = Integer.MAX_VALUE;
        int maxScan = 32;

        for (int i = 1; i <= maxScan; i++) {
            mutable.set(pos.getX(), pos.getY() + i, pos.getZ());
            BlockState state = client.world.getBlockState(mutable);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                solidBlocks++;
                if (firstSolidDistance == Integer.MAX_VALUE) {
                    firstSolidDistance = i;
                }
            }
        }

        return new Overhead(solidBlocks, firstSolidDistance);
    }

    private record Overhead(int solidBlocks, int firstSolidDistance) {
    }
}
