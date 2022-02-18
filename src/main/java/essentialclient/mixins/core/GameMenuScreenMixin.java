package essentialclient.mixins.core;

import essentialclient.clientrule.ClientRules;
import essentialclient.gui.ConfigScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
	protected GameMenuScreenMixin(Text title) {
		super(title);
	}

	@ModifyConstant(method = "initWidgets", constant = @Constant(intValue = 120))
	private int pushLimit(int original) {
		return ClientRules.ESSENTIAL_CLIENT_BUTTON.getValue() ? 144 : original;
	}

	@Inject(method = "initWidgets", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		if (this.client == null || !ClientRules.ESSENTIAL_CLIENT_BUTTON.getValue()) {
			return;
		}
		ButtonWidget buttonWidget = this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 120 - 16, 204, 20, new LiteralText("Essential Client Menu"), (b) -> this.client.setScreen(new ConfigScreen(this))));
		buttonWidget.active = true;
	}
}
