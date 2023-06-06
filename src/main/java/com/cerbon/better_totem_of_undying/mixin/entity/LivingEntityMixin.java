package com.cerbon.better_totem_of_undying.mixin.entity;

import com.cerbon.better_totem_of_undying.config.BTUCommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, net.minecraftforge.common.extensions.IForgeLivingEntity  {

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow public abstract boolean addEffect(MobEffectInstance pEffectInstance);

    @Shadow public abstract void setHealth(float pHealth);

    @Shadow public abstract boolean removeAllEffects();

    @Shadow public abstract boolean isInWall();

    @Inject(method = "checkTotemDeathProtection", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
            cancellable = true)
    public void improveTotemOfUndying(DamageSource pDamageSource, CallbackInfoReturnable<Boolean> cir) {
        if (BTUCommonConfigs.IS_MOD_ENABLED.get()) {
            boolean isFireResistanceEffectEnabled = BTUCommonConfigs.ENABLE_FIRE_RESISTANCE.get();
            int fireResistanceEffectDuration = BTUCommonConfigs.FIRE_RESISTANCE_DURATION.get();

            boolean isRegenerationEffectEnabled = BTUCommonConfigs.ENABLE_REGENERATION.get();
            int regenerationEffectDuration = BTUCommonConfigs.REGENERATION_DURATION.get();
            int regenerationEffectAmplifier = BTUCommonConfigs.REGENERATION_AMPLIFIER.get();

            boolean isAbsorptionEffectEnabled = BTUCommonConfigs.ENABLE_ABSORPTION.get();
            int absorptionEffectDuration = BTUCommonConfigs.ABSORPTION_DURATION.get();
            int absorptionEffectAmplifier = BTUCommonConfigs.ABSORPTION_AMPLIFIER.get();

            boolean isWaterBreathingEffectEnabled = BTUCommonConfigs.ENABLE_WATER_BREATHING.get();
            int waterBreathingEffectDuration = BTUCommonConfigs.WATER_BREATHING_DURATION.get();

            boolean isIncreaseFoodLevelEnabled = BTUCommonConfigs.ENABLE_INCREASE_FOOD_LEVEL.get();
            int minimumFoodLevel = BTUCommonConfigs.MINIMUM_FOOD_LEVEL.get();
            int setFoodLevel = BTUCommonConfigs.SET_FOOD_LEVEL.get();

            boolean isDestroyBlocksWhenSuffocatingEnabled = BTUCommonConfigs.DESTROY_BLOCKS_WHEN_SUFFOCATING.get();

            this.setHealth(BTUCommonConfigs.SET_HEALTH.get());

            if (BTUCommonConfigs.REMOVE_ALL_EFFECTS.get()) {
                this.removeAllEffects();
            }

            if (BTUCommonConfigs.APPLY_EFFECTS_ONLY_WHEN_NEEDED.get()) {
                if (this.isOnFire() && isFireResistanceEffectEnabled) {
                    this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, fireResistanceEffectDuration, 0));
                }
                if (this.isInWaterOrBubble() && isWaterBreathingEffectEnabled) {
                    this.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, waterBreathingEffectDuration, 0));
                }
            } else {
                if (isFireResistanceEffectEnabled) {
                    this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, fireResistanceEffectDuration, 0));
                }
                if (isWaterBreathingEffectEnabled) {
                    this.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, waterBreathingEffectDuration, 0));
                }
            }

            if (isRegenerationEffectEnabled) {
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenerationEffectDuration, regenerationEffectAmplifier));
            }
            if (isAbsorptionEffectEnabled) {
                this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, absorptionEffectDuration, absorptionEffectAmplifier));
            }
            if (this.getType() == EntityType.PLAYER && isIncreaseFoodLevelEnabled) {
                Player player = (Player) (Object) this;
                int foodLevel = player.getFoodData().getFoodLevel();
                if (foodLevel <= minimumFoodLevel) {
                    player.getFoodData().setFoodLevel(setFoodLevel);
                }
            }
            if (this.isInWall() && isDestroyBlocksWhenSuffocatingEnabled) {
                Level level = this.level;
                BlockPos entityPosition = this.blockPosition();
                BlockState blockAtEntityPosition = level.getBlockState(entityPosition);
                BlockState blockAboveEntityPosition = level.getBlockState(entityPosition.above());

                if (blockAtEntityPosition.getBlock() != Blocks.BEDROCK && blockAboveEntityPosition.getBlock() != Blocks.BEDROCK) {
                    if (level.getBlockState(entityPosition.above(2)).getBlock() instanceof FallingBlock) {
                        int i = 2;
                        while (true){
                            if (level.getBlockState(entityPosition.above(i)).getBlock() instanceof FallingBlock){
                                level.destroyBlock(entityPosition.above(i), true);
                                i++;
                            }else{
                                break;
                            }
                        }
                    }
                    level.destroyBlock(entityPosition, true);
                    level.destroyBlock(entityPosition.above(), true);
                }
            }
            this.level.broadcastEntityEvent(this, (byte) 35);
            cir.setReturnValue(true);
        }
    }
}
