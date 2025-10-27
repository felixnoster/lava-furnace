package com.example.lavafurnace.mixin;

import com.example.lavafurnace.mixin.accessor.AbstractFurnaceBlockEntityAccessor;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity {

    private static final Logger LOG = LoggerFactory.getLogger("LavaFurnace");
    private static final int RADIUS = 4;
    private static final int TOP_UP_TICKS = 200;
    private static final boolean REQUIRE_INPUT = true;

    @Inject(method = "tick", at = @At("TAIL"))
    private static void lavafurnace$afterTick(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity be, CallbackInfo ci) {
        if (world.isClient()) return;

        MinecraftServer server = world.getServer();
        if (server == null) return;
        if (server.isDedicated()) return; // singleplayer-only

        if (!state.isOf(Blocks.FURNACE)) return;
        if (REQUIRE_INPUT && be.getStack(0).isEmpty()) return;
        if (!isNearLava(world, pos, RADIUS)) return;

        AbstractFurnaceBlockEntityAccessor acc = (AbstractFurnaceBlockEntityAccessor) (Object) be;
        if (acc.getBurnTime() < (TOP_UP_TICKS / 2)) {
            acc.setBurnTime(TOP_UP_TICKS);
            if (server.getTicks() % 100 == 0) {
                LOG.info("[LavaFurnace] Topped up burnTime at {} {}", pos.getX(), pos.getZ());
            }
        }

        if (state.contains(AbstractFurnaceBlock.LIT) && !state.get(AbstractFurnaceBlock.LIT)) {
            world.setBlockState(pos, state.with(AbstractFurnaceBlock.LIT, true), 3);
        }
    }

    private static boolean isNearLava(World world, BlockPos pos, int r) {
        if (isLava(world, pos.up())) return true;
        if (isLava(world, pos.down())) return true;
        if (isLava(world, pos.north())) return true;
        if (isLava(world, pos.south())) return true;
        if (isLava(world, pos.east())) return true;
        if (isLava(world, pos.west())) return true;

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    if (isLava(world, pos.add(dx, dy, dz))) return true;
                }
            }
        }
        return false;
    }

    private static boolean isLava(World world, BlockPos p) {
        BlockState bs = world.getBlockState(p);
        return bs.isOf(Blocks.LAVA) || bs.isOf(Blocks.LAVA_CAULDRON);
    }
}
