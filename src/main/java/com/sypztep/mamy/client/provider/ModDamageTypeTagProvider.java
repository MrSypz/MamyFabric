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
		// FIRE DAMAGE - Just reference vanilla IS_FIRE tag
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.FIRE_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_FIRE); // DRY: Use vanilla's comprehensive fire tag

		// ==========================================
		// ELECTRIC DAMAGE - Just reference vanilla IS_LIGHTNING tag
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.ELECTRIC_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_LIGHTNING); // DRY: Use vanilla's lightning tag

		// ==========================================
		// PROJECTILE DAMAGE - Just reference vanilla IS_PROJECTILE tag
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.PROJECTILE_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_PROJECTILE);     // DRY: Use vanilla's projectile tag

		// ==========================================
		// WATER DAMAGE - Combine vanilla water-related tags
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.WATER_DAMAGE)
				.addOptionalTag(DamageTypeTags.IS_DROWNING);
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
				.addOptionalTag(DamageTypeTags.IS_PLAYER_ATTACK)  // DRY: Use vanilla player attack
				.add(DamageTypes.MOB_ATTACK)                      // ADD: Mob melee
				.add(DamageTypes.MOB_ATTACK_NO_AGGRO)             // ADD: Passive mob attacks
				.add(DamageTypes.STING)                           // ADD: Bee stings
				.add(DamageTypes.THORNS)                          // ADD: Thorns enchantment
				.add(DamageTypes.SWEET_BERRY_BUSH)                // ADD: Contact damage
				.add(DamageTypes.CACTUS);                         // ADD: Cactus damage

		// ==========================================
		// HOLY DAMAGE - Pure magical/supernatural (no vanilla equivalent)
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.HOLY_DAMAGE);

		// ==========================================
		// MAGIC DAMAGE - Your custom magic system
		// ==========================================
		getOrCreateTagBuilder(ModTags.DamageTags.MAGIC_DAMAGE)
				.add(DamageTypes.THORNS)
				.addOptionalTag(DamageTypeTags.WITCH_RESISTANT_TO); // DRY: Use vanilla witch resistances
	}
}