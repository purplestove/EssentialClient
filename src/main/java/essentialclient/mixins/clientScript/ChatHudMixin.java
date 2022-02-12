package essentialclient.mixins.clientScript;

import essentialclient.utils.interfaces.ChatHudAccessor;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ChatHud.class)
public class ChatHudMixin implements ChatHudAccessor {

	@Final
	@Shadow
	private List<ChatHudLine<Text>> messages;

	@Override
	public List<ChatHudLine<Text>> getMessages() {
		return this.messages;
	}
}
