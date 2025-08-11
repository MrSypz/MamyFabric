package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.common.init.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTagProvider extends FabricTagProvider<DamageType> {
	public ModDamageTypeTagProvider(FabricDataOutput output) {
		super(output, RegistryKeys.DAMAGE_TYPE, CompletableFuture.supplyAsync(BuiltinRegistries::createWrapperLookup));
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
		// ==========================================
		// MELEE DAMAGE - Close combat attacks
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.MELEE_DAMAGE)
				.add(DamageTypes.MOB_ATTACK)          // Melee from mobs
				.add(DamageTypes.PLAYER_ATTACK)       // Melee from players
				.addOptionalTag(ModTags.DamageTags.DOUBLE_ATTACK)
				.add(DamageTypes.STING);              // Bee stings (close range)

		// ==========================================
		// MAGIC DAMAGE - Magical/supernatural forces
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.MAGIC_DAMAGE)
				.add(DamageTypes.WITHER_SKULL)        // Wither skull projectiles
				.add(DamageTypes.DRAGON_BREATH)       // Ender dragon breath
				.add(DamageTypes.WITHER)              // Wither effect
				.addOptionalTag(DamageTypeTags.WITCH_RESISTANT_TO); // Vanilla magic tag

		// ==========================================
		// FIRE DAMAGE - Heat and flame based
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.FIRE_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_FIRE); // Vanilla fire tag

		// ==========================================
		// PROJECTILE DAMAGE - Ranged attacks
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.PROJECTILE_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_PROJECTILE); // Vanilla projectile tag

	}
}
