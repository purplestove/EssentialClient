package essentialclient.mixins.functions;

import essentialclient.clientscript.events.MinecraftScriptEvents;
import essentialclient.clientscript.values.ItemStackValue;
import essentialclient.clientscript.values.PlayerValue;
import me.senseiwells.arucas.values.NumberValue;
import me.senseiwells.arucas.values.StringValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	private ClientWorld world;

	@Final
	@Shadow
	private MinecraftClient client;

	@Inject(method = "onHealthUpdate", at = @At("HEAD"))
	private void onHealthUpdate(HealthUpdateS2CPacket packet, CallbackInfo ci) {
		MinecraftScriptEvents.ON_HEALTH_UPDATE.run(NumberValue.of(packet.getHealth()));
	}

	@Inject(method = "onEntityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;showFloatingItem(Lnet/minecraft/item/ItemStack;)V"))
	private void onTotem(CallbackInfo ci) {
		MinecraftScriptEvents.ON_TOTEM.run();
	}

	@Inject(method = "onGameStateChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;setGameMode(Lnet/minecraft/world/GameMode;)V"))
	private void onGamemodeChange(GameStateChangeS2CPacket packet, CallbackInfo ci) {
		MinecraftScriptEvents.ON_GAMEMODE_CHANGE.run(List.of(StringValue.of(GameMode.byId((int) packet.getValue()).getName())));
	}

	@Inject(method = "onItemPickupAnimation", at = @At("HEAD"))
	private void onPickUp(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
		Entity entity = this.world.getEntityById(packet.getEntityId());
		LivingEntity livingEntity = (LivingEntity) this.world.getEntityById(packet.getCollectorEntityId());
		if (entity != null && this.client.player != null && this.client.player.equals(livingEntity) && entity instanceof ItemEntity itemEntity) {
			MinecraftScriptEvents.ON_PICK_UP_ITEM.run(new ItemStackValue(itemEntity.getStack()));
		}
	}

	@Inject(method = "onPlayerRespawn", at = @At("TAIL"))
	private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
		MinecraftScriptEvents.ON_RESPAWN.run(List.of(new PlayerValue(this.client.player)));
	}

	@Inject(method = "onGameMessage", at = @At("HEAD"))
	private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
		MinecraftScriptEvents.ON_RECEIVE_MESSAGE.run(StringValue.of(packet.getMessage().getString()));
	}

	@Inject(method = "onPlayerList", at = @At("HEAD"))
	public void onPlayerList(PlayerListS2CPacket packet, CallbackInfo info) {
		switch (packet.getAction()) {
			case ADD_PLAYER -> packet.getEntries().forEach(entry -> MinecraftScriptEvents.ON_PLAYER_JOIN.run(StringValue.of(entry.getProfile().getName())));
			case REMOVE_PLAYER -> packet.getEntries().forEach(entry -> MinecraftScriptEvents.ON_PLAYER_LEAVE.run(StringValue.of(entry.getProfile().getName())));
		}
	}
}
