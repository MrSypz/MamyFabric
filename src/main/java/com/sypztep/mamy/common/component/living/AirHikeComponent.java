package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.network.server.AirHikePayloadC2S;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundEvents;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public final class AirHikeComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;

    // Air jump state
    private short jumpsLeft = 0;
    private final short maxJumps = 2;
    private boolean wasOnGround = true;
    private int ticksInAir = 0;
    private short jumpCooldown = 0;

    private boolean wasJumping = false;
    public AirHikeComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        jumpsLeft = tag.getShort("JumpsLeft");
        wasOnGround = tag.getBoolean("WasOnGround");
        ticksInAir = tag.getShort("TicksInAir");
        jumpCooldown = tag.getShort("JumpCooldown");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("JumpsLeft", jumpsLeft);
        tag.putBoolean("WasOnGround", wasOnGround);
        tag.putInt("TicksInAir", ticksInAir);
        tag.putShort("JumpCooldown", jumpCooldown);
    }

    @Override
    public void tick() {
        if (player.isOnGround() && !wasOnGround) {
            jumpsLeft = maxJumps;
            wasOnGround = true;
            ticksInAir = 0;
        } else if (!player.isOnGround()) {
            wasOnGround = false;
            ticksInAir++;
        }
        if (jumpCooldown > 0) jumpCooldown--;
    }

    @Override
    public void clientTick() {
        tick();

        if (player.jumping && !wasJumping && canUseAirJump() && !player.isOnGround() && LivingEntityUtil.canPerformJump(player)) {
            performAirJump();
            addAirhikeParticles(player);
            AirHikePayloadC2S.send();
        }
        wasJumping = player.jumping;
    }

    public boolean canUseAirJump() {
        return jumpsLeft > 0 && !player.isOnGround() && ticksInAir >= 4 && jumpCooldown == 0;
    }

    public boolean isAirBorn() {
        return ticksInAir > 3;
    }
    public void performAirJump() {
        if (!canUseAirJump()) return;

        player.jump();
        player.setVelocity(player.getVelocity().getX(), player.getVelocity().getY() * 1.5f, player.getVelocity().getZ());
        player.addExhaustion(0.1f);
        jumpCooldown = 6;
        jumpsLeft--;

        player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 0.7f, (float) (1.5 + player.getRandom().nextGaussian() / 20));
        player.playSound(SoundEvents.BLOCK_SAND_PLACE, 0.8f, (float) (1.2f + player.getRandom().nextGaussian() / 20));
    }

    public static void addAirhikeParticles(Entity entity) {
        if (MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson() || entity != MinecraftClient.getInstance().cameraEntity)
            entity.getWorld().addParticle(ModParticles.AIRHIKE, entity.getX(), entity.getY(), entity.getZ(), 0, -0.1, 0);
    }

    public float getAirControlMultiplier() {
        return 1.5f;
    }

    public int getJumpsLeft() {
        return jumpsLeft;
    }

    public int getMaxJumps() {
        return maxJumps;
    }
}