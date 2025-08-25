package com.sypztep.mamy.mixin.vanilla.rebalancestatuseffects;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StatusEffect.class)
public class StatusEffectMixin {
	@Unique
	private static final Identifier STRENGTH_ID = Identifier.ofVanilla("effect.strength");
	@Unique
	private static final Identifier WEAKNESS_ID = Identifier.ofVanilla("effect.weakness");
	@Unique
	private static final Identifier SPEED_ID = Identifier.ofVanilla("effect.speed");

	@ModifyVariable(method = "addAttributeModifier", at = @At("HEAD"), argsOnly = true)
	private double rebalanceStatusEffects(double value, RegistryEntry<EntityAttribute> attribute, Identifier id) {
		if (attribute == EntityAttributes.GENERIC_ATTACK_DAMAGE) {
			if (id.equals(STRENGTH_ID)) {
				return 0.5f;
			} else if (id.equals(WEAKNESS_ID)) {
				return -1;
			}
		}
		if (attribute == EntityAttributes.GENERIC_MOVEMENT_SPEED) {
			if (id.equals(SPEED_ID)) {
				return 0.1f;
			}
		}
		return value;
	}
}
