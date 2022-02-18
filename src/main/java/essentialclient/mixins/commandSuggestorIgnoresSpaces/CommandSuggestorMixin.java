package essentialclient.mixins.commandSuggestorIgnoresSpaces;

import essentialclient.clientrule.ClientRules;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandSuggestor.class)
public abstract class CommandSuggestorMixin {
	@Redirect(method = "refresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;getCursor()I"))
	private int onGetCursor(TextFieldWidget textFieldWidget) {
		int current = textFieldWidget.getCursor();
		boolean hasSpace = false;
		String message = textFieldWidget.getText();
		if (current == 0 || !message.startsWith("/") || !ClientRules.COMMAND_SUGGESTOR_IGNORES_SPACES.getValue()) {
			return current;
		}
		current--;
		while (message.charAt(current) == ' ') {
			hasSpace = true;
			current--;
		}
		current += hasSpace ? 2 : 1;
		return current;
	}
}
