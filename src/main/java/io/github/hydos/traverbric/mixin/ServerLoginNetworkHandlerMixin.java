package io.github.hydos.traverbric.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.hydos.traverbric.BungeeConnectionExtra;
import io.github.hydos.traverbric.TraverBric;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow
    @Final
    public ClientConnection connection;

    @Shadow
    private GameProfile profile;

    @Shadow
    private ServerLoginNetworkHandler.State state;

    @Shadow
    private String field_6756;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private byte[] nonce;

    @Shadow
    public abstract void disconnect(String string);

    public void initUUID() {
        UUID uuid;
        if (((BungeeConnectionExtra) connection).getSpoofedUUID() != null) {
            uuid = ((BungeeConnectionExtra) connection).getSpoofedUUID();
        } else {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.profile.getName()).getBytes(Charsets.UTF_8));
        }
        this.profile = new GameProfile(uuid, this.profile.getName());
        if (((BungeeConnectionExtra) connection).getSpoofedUUID() != null) {
            for (Property property : ((BungeeConnectionExtra) connection).getSpoofedProfile()) {
                this.profile.getProperties().put(property.getName(), property);
            }
        }
    }

    /**
     * @author hydos
     * @reason bungee
     */
    @Overwrite
    public void onHello(LoginHelloC2SPacket packet) {
        Validate.validState(this.state == ServerLoginNetworkHandler.State.HELLO, "Unexpected hello packet");
        this.profile = packet.getProfile();
        if (this.server.isOnlineMode() && !this.connection.isLocal()) {
            this.state = ServerLoginNetworkHandler.State.KEY;
            this.connection.send(new LoginHelloS2CPacket(this.field_6756, this.server.getKeyPair().getPublic(), this.nonce));
        } else {
            initUUID();
            new Thread(() -> {
                try {
                    this.state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;
                } catch (Exception e) {
                    disconnect("Failed to verify username!");
                    TraverBric.LOGGER.warn("Exception verifying " + profile.getName(), e);
                }
            }).start();
        }

    }
}
