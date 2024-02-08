//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

import net.minecraft.world.level.GameRules;


@Mixin(GameRules.class)
public interface GameRulesAccessor
{
    @Accessor
    static Map<GameRules.Key<?>, GameRules.Type<?>> getGAME_RULE_TYPES()
    {
        throw new UnsupportedOperationException();
    }
}
