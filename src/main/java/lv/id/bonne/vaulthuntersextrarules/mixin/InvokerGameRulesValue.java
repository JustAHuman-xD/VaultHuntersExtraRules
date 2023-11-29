package lv.id.bonne.vaulthuntersextrarules.mixin;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Interface for invoking private/protected methods of GameRules.Value
 */
@Mixin(GameRules.Value.class)
public interface InvokerGameRulesValue {
    @Invoker
    void invokeDeserialize(String str);
}
