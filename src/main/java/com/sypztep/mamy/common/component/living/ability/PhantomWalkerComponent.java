package com.sypztep.mamy.common.component.living.ability;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.payload.AirHikePayloadC2S;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundEvents;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

/**
 * AGI 75 - AirHike: Double Jump Component
 */
public class PhantomWalkerComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;

    // Air jump state
    private short jumpsLeft = 0;
    private short maxJumps = 0;
    private boolean wasOnGround = true;
    private int ticksInAir = 0;
    private short jumpCooldown = 0;

    private boolean wasJumping = false;
    // Air control state
    private boolean hasAirControl = false;

    public PhantomWalkerComponent(PlayerEntity player) {
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
        maxJumps = getMaxAirJumps();
        hasAirControl = hasAirControl();

        boolean hasAirJump = maxJumps > 0;

        if (hasAirJump) {
            if (player.isOnGround() && !wasOnGround) {
                jumpsLeft = maxJumps;
                wasOnGround = true;
                ticksInAir = 0;
            } else if (!player.isOnGround()) {
                wasOnGround = false;
                ticksInAir++;
            }
            if (jumpCooldown > 0) {
                jumpCooldown--;
            }
        } else {
            jumpsLeft = 0;
            wasOnGround = true;
            ticksInAir = 0;
            jumpCooldown = 0;
        }
    }

    @Override
    public void clientTick() {
        tick();

        if (maxJumps > 0 && player.jumping && !wasJumping && canUseAirJump() && !player.isOnGround() && LivingEntityUtil.canPerformJump(player)) {
            performAirJump();
            addAirhikeParticles(player);
            AirHikePayloadC2S.send();
        }
        wasJumping = player.jumping;
    }

    public boolean canUseAirJump() {
        return jumpsLeft > 0 && !player.isOnGround() && ticksInAir >= 6 && jumpCooldown == 0;
    }

    public boolean isAirBorn() {
        return ticksInAir > 3;
    }
    public void performAirJump() {
        if (!canUseAirJump()) return;

        player.jump();
        player.setVelocity(player.getVelocity().getX(), player.getVelocity().getY() * 1.5f, player.getVelocity().getZ());
        player.addExhaustion(0.1f);
        // Delay jump prevent double jump by accident
        jumpCooldown = 10;
        // Update state
        jumpsLeft--;

        // Effects
        player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 0.7f, (float) (1.5 + player.getRandom().nextGaussian() / 20));
        player.playSound(SoundEvents.BLOCK_SAND_PLACE, 0.8f, (float) (1.2f + player.getRandom().nextGaussian() / 20));
    }

    public static void addAirhikeParticles(Entity entity) {
        if (MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson() || entity != MinecraftClient.getInstance().cameraEntity)
            entity.getWorld().addParticle(ModParticles.AIRHIKE, entity.getX(), entity.getY(), entity.getZ(), 0, -0.1, 0);
    }

    public float getAirControlMultiplier() {
        return hasAirControl ? 3.0f : 1.0f;
    }

    public int getJumpsLeft() {
        return jumpsLeft;
    }

    public int getMaxJumps() {
        return maxJumps;
    }

    private short getMaxAirJumps() {
        return hasAirHike() ? 2 : (short) (hasAirControl() ? 1 : 0);
    }
    public boolean hasAirControl() {
        return PassiveAbilityManager.isActive(player, ModPassiveAbilities.WIND_WALKER);
    }
    public boolean hasAirHike() {
        return PassiveAbilityManager.isActive(player, ModPassiveAbilities.PHANTOM_WALKER);
    }
}