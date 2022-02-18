package essentialclient.mixins.betterAccurateBlockPlacement;

import com.mojang.authlib.GameProfile;
import essentialclient.clientrule.ClientRules;
import essentialclient.feature.BetterAccurateBlockPlacement;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {
	private ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	@Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 2), require = 0)
	private void onSendPacketVehicle(ClientPlayNetworkHandler clientPlayNetworkHandler, Packet<?> packet) {
		if (!ClientRules.BETTER_ACCURATE_BLOCK_PLACEMENT.getValue()) {
			clientPlayNetworkHandler.sendPacket(packet);
			return;
		}
		Vec3d vec3d = this.getVelocity();
		clientPlayNetworkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
			vec3d.x,
			-999,
			vec3d.y,
			BetterAccurateBlockPlacement.fakeYaw,
			BetterAccurateBlockPlacement.fakePitch,
			this.isOnGround()
		));
	}

	@Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 3), require = 0)
	private void onSendPacketAll(ClientPlayNetworkHandler clientPlayNetworkHandler, Packet<?> packet) {
		if (!ClientRules.BETTER_ACCURATE_BLOCK_PLACEMENT.getValue()) {
			clientPlayNetworkHandler.sendPacket(packet);
			return;
		}
		clientPlayNetworkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
			this.getX(),
			this.getY(),
			this.getZ(),
			BetterAccurateBlockPlacement.fakeYaw,
			BetterAccurateBlockPlacement.fakePitch,
			this.isOnGround()
		));
	}

	@Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 5), require = 0)
	private void onSendPacketLook(ClientPlayNetworkHandler clientPlayNetworkHandler, Packet<?> packet) {
		if (!ClientRules.BETTER_ACCURATE_BLOCK_PLACEMENT.getValue()) {
			clientPlayNetworkHandler.sendPacket(packet);
			return;
		}
		clientPlayNetworkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
			BetterAccurateBlockPlacement.fakeYaw,
			BetterAccurateBlockPlacement.fakePitch,
			this.isOnGround()
		));
	}
}
