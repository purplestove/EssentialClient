package essentialclient.mixins.core;

import essentialclient.clientscript.ClientScript;
import essentialclient.clientscript.events.MinecraftScriptEvents;
import essentialclient.feature.EssentialCarpetClient;
import essentialclient.feature.chunkdebug.ChunkClientNetworkHandler;
import essentialclient.feature.chunkdebug.ChunkDebugScreen;
import essentialclient.feature.chunkdebug.ChunkHandler;
import essentialclient.utils.interfaces.MinecraftClientInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin extends ReentrantThreadExecutor<Runnable> implements MinecraftClientInvoker {

	@Shadow
	public ClientPlayerEntity player;

	public MinecraftClientMixin(String string) {
		super(string);
	}

	@Shadow
	private void doAttack() { }

	@Shadow
	private void doItemUse() { }

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
	private void onLeaveWorld(Screen screen, CallbackInfo ci) {
		MinecraftScriptEvents.ON_DISCONNECT.run();

		EssentialCarpetClient.serverIsCarpet = false;
		EssentialCarpetClient.carpetRules.clear();

		ChunkClientNetworkHandler.chunkDebugAvailable = false;
		ChunkHandler.clearAllChunks();
		ChunkDebugScreen.chunkGrid = null;
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
	private void onDisconnect(Screen screen, CallbackInfo ci) {
		ClientScript.getInstance().stopScript();
	}

	@Override
	public void rightClickMouseAccessor() {
		this.execute(this::doItemUse);
	}

	@Override
	public void leftClickMouseAccessor() {
		this.execute(this::doAttack);
	}
}
