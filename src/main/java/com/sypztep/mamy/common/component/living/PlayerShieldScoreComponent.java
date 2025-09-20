package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public final class PlayerShieldScoreComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;
    private double maxShieldScore;
    private double currentShieldScore;
    private boolean isBlocking;
    private int regenerationCooldown;

    private static final int REGEN_DELAY_TICKS = 60; // 3 seconds at 20 TPS
    private static final double REGEN_RATE = 0.5; // Points per tick when regenerating

    public PlayerShieldScoreComponent(PlayerEntity player) {
        this.player = player;
        this.maxShieldScore = 0.0;
        this.currentShieldScore = 0.0;
        this.isBlocking = false;
        this.regenerationCooldown = 0;
    }

    @Override
    public void tick() {
        if (player.getWorld().isClient) return;

        boolean currentlyBlocking = isUsingShield();
        if (this.isBlocking && !currentlyBlocking) {
            stopBlocking();
        }

        if (currentlyBlocking && regenerationCooldown == 0) regenerationCooldown = 40;

        double newMaxScore = calculateMaxShieldScore();
        if (Math.abs(this.maxShieldScore - newMaxScore) > 0.01) {
            double ratio = this.maxShieldScore > 0 ? this.currentShieldScore / this.maxShieldScore : 0;
            this.maxShieldScore = newMaxScore;
            this.currentShieldScore = this.maxShieldScore * ratio;
            sync();
        }

        if (regenerationCooldown > 0) {
            regenerationCooldown--;
        } else if (currentShieldScore < maxShieldScore && !isBlocking) {
            double oldScore = currentShieldScore;
            currentShieldScore = Math.min(maxShieldScore, currentShieldScore + REGEN_RATE);

            if (Math.abs(oldScore - currentShieldScore) > 0.01) {
                sync();
            }
        }
    }

    private double calculateMaxShieldScore() {
        double health = player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
        double armor = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
        double damageReduction = player.getAttributeValue(ModEntityAttributes.DAMAGE_REDUCTION);
        return health + armor + (damageReduction * 100);
    }

    public void startBlocking() {
        this.isBlocking = true;
        if (currentShieldScore <= 0) {
            currentShieldScore = 0;
        }
    }

    public void stopBlocking() {
        this.isBlocking = false;
    }

    public boolean canBlock() {
        return currentShieldScore > 0;
    }

    public void consumeShieldScore(double damage, boolean penalty) {
        if (currentShieldScore > 0) {
            currentShieldScore = Math.max(0, currentShieldScore - damage);
            if (penalty)
                regenerationCooldown = 200;
            else regenerationCooldown = REGEN_DELAY_TICKS;
            sync();
        }
    }

    public double getShieldScorePercentage() {
        return maxShieldScore > 0 ? (currentShieldScore / maxShieldScore) * 100.0 : 0.0;
    }

    public double getCurrentShieldScore() {
        return currentShieldScore;
    }

    public double getMaxShieldScore() {
        return maxShieldScore;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    private boolean isUsingShield() {
        return player.isUsingItem() && player.getActiveItem().getItem() instanceof ShieldItem;
    }

    public void setRegenerationCooldown(int regenerationCooldown) {
        this.regenerationCooldown = regenerationCooldown;
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        this.maxShieldScore = nbt.getDouble("maxShieldScore");
        this.currentShieldScore = nbt.getDouble("currentShieldScore");
        this.isBlocking = nbt.getBoolean("isBlocking");
        this.regenerationCooldown = nbt.getInt("regenerationCooldown");
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbt.putDouble("maxShieldScore", this.maxShieldScore);
        nbt.putDouble("currentShieldScore", this.currentShieldScore);
        nbt.putBoolean("isBlocking", this.isBlocking);
        nbt.putInt("regenerationCooldown", this.regenerationCooldown);
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeDouble(this.maxShieldScore);
        buf.writeDouble(this.currentShieldScore);
    }


    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        this.maxShieldScore = buf.readDouble();
        this.currentShieldScore = buf.readDouble();
    }

    private void sync() {
        ModEntityComponents.PLAYERSHIELDSCORE.sync(player);
    }
}