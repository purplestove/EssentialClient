package essentialclient.mixins.mouseScrollRules;

import essentialclient.gui.clientrule.ClientRules;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MouseMixin {
    @ModifyConstant(method = "onMouseScroll", constant = @Constant(floatValue = 0.2F))
    private float newScrollLimit(float originalFloat) {
        return ClientRules.INCREASESPECTATORSCROLLSPEED.getBoolean() ? 10.0F : originalFloat;
    }
    @ModifyConstant(method = "onMouseScroll", constant = @Constant(floatValue = 0.005F))
    private float newSensitivtyLimit(float originalFloat) {
        int newSensitivity = ClientRules.INCREASESPECTATORSCROLLSENSITIVITY.getInt();
        return newSensitivity > 0 ? originalFloat * newSensitivity : originalFloat;
    }
    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    private void onScrollHotbar(PlayerInventory playerInventory, double scrollAmount) {
        if (ClientRules.DISABLEHOTBARSCROLLING.getBoolean())
            return;
        playerInventory.scrollInHotbar(scrollAmount);
    }
}