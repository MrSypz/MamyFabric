package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.common.init.ModDamageTypes;
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
		// ADDITIONAL USEFUL VANILLA TAGS
		// ==========================================

		// Bypasses Armor - Damage that ignores armor completely
		getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR);

		// Bypasses Shield - Damage that goes through shields
		getOrCreateTagBuilder(DamageTypeTags.BYPASSES_SHIELD)
				.addOptional(ModDamageTypes.SHOCKWAVE_FLAME)
				.addOptional(ModDamageTypes.BASHING_BLOW);

		// Bypasses Enchantments - Damage that ignores Protection enchants
		getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ENCHANTMENTS)
				.addOptional(ModDamageTypes.SHOCKWAVE_FLAME);

		// Bypasses Cooldown - Damage that doesn't trigger hurt immunity
		getOrCreateTagBuilder(DamageTypeTags.BYPASSES_COOLDOWN)
				.add(DamageTypes.MOB_ATTACK)
				.add(DamageTypes.MOB_PROJECTILE)
				.add(DamageTypes.MOB_ATTACK_NO_AGGRO)
				.add(DamageTypes.ARROW)
				.addOptional(ModDamageTypes.HOLY)
				.addOptional(ModDamageTypes.ARROW_RAIN)
				.addOptional(ModDamageTypes.DOUBLE_ATTACK)
				.addOptional(ModDamageTypes.MAGIC_ARROW)
				.addOptional(ModDamageTypes.FIREBALL)
				.addOptional(ModDamageTypes.LIGHTING)
				.addOptional(ModDamageTypes.SHOCKWAVE_FLAME);

		// No Impact - Damage that doesn't cause knockback/hitstun
		getOrCreateTagBuilder(DamageTypeTags.NO_IMPACT)
				.addOptional(ModDamageTypes.DOUBLE_ATTACK)
				.addOptional(ModDamageTypes.HOLY)
				.addOptional(ModDamageTypes.MAGIC_ARROW)
				.addOptional(ModDamageTypes.LIGHTING);

		// No Knockback - Damage that doesn't push entities
		getOrCreateTagBuilder(DamageTypeTags.NO_KNOCKBACK)
				.addOptional(ModDamageTypes.DOUBLE_ATTACK)
				.addOptional(ModDamageTypes.HOLY)
				.addOptional(ModDamageTypes.ARROW_RAIN)
				.addOptional(ModDamageTypes.MAGIC_ARROW)
				.addOptional(ModDamageTypes.LIGHTING);

		// ==========================================
		// FIRE DAMAGE - Just reference vanilla IS_FIRE tag
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.FIRE_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_FIRE)
				.addOptional(ModDamageTypes.ENERGY_BREAK)
				.addOptional(ModDamageTypes.FIREBALL);

		// ==========================================
		// ELECTRIC DAMAGE - Just reference vanilla IS_LIGHTNING tag
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.ELECTRIC_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_LIGHTNING)
				.addOptional(ModDamageTypes.LIGHTING);

		// ==========================================
		// PROJECTILE DAMAGE - Just reference vanilla IS_PROJECTILE tag
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.PROJECTILE_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_PROJECTILE)
				.addOptional(ModDamageTypes.ARROW_RAIN);

		// ==========================================
		// WATER DAMAGE - Combine vanilla water-related tags
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.WATER_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_DROWNING)
				;
		// ==========================================
		// COLD DAMAGE - Combine vanilla cold-related tags
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.COLD_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_FREEZING);

		// ==========================================
		// WIND DAMAGE - Combine vanilla fall + kinetic damage
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.WIND_DAMAGE)
				.add(DamageTypes.WIND_CHARGE)
				.add(DamageTypes.SONIC_BOOM)
				.add(DamageTypes.DRY_OUT);

		// ==========================================
		// MELEE DAMAGE - Add vanilla player attack + close combat
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.MELEE_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_PLAYER_ATTACK)
				.add(DamageTypes.MOB_ATTACK)
				.add(DamageTypes.MOB_ATTACK_NO_AGGRO)
				.add(DamageTypes.STING)
				.add(DamageTypes.THORNS)
				.add(DamageTypes.SWEET_BERRY_BUSH)
				.add(DamageTypes.CACTUS)
				// ADD YOUR SKILL DAMAGE TYPES TO MELEE TAG
				.addOptional(ModDamageTypes.DOUBLE_ATTACK)
				.addOptional(ModDamageTypes.BASHING_BLOW);

		// ==========================================
		// HOLY DAMAGE - Pure magical/supernatural (no vanilla equivalent)
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.HOLY_DAMAGE)
				.addOptional(ModDamageTypes.HOLY);

		// ==========================================
		// MAGIC DAMAGE - Your custom magic system
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.MAGIC_DAMAGE)
				.add(DamageTypes.THORNS)
				.addOptionalTag(DamageTypeTags.WITCH_RESISTANT_TO)
				.addOptional(ModDamageTypes.MAGIC_ARROW);
	}
}