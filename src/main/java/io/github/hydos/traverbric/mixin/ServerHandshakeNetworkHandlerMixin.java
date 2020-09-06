package io.github.hydos.traverbric.mixin;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import io.github.hydos.traverbric.BungeeConnectionExtra;
import io.github.hydos.traverbric.TraverBric;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {

    @Shadow
    @Final
    private ClientConnection connection;

    @Shadow
    @Final
    private MinecraftServer server;

    private final Gson GSON = new Gson();

    /**
     * @author hydos
     * @reason Bungee
     */
    @Overwrite
    public void onHandshake(HandshakeC2SPacket packet) {
        switch (packet.getIntendedState()) {
            case LOGIN:
                this.connection.setState(NetworkState.LOGIN);
                LiteralText literalText2;
                if (packet.getProtocolVersion() > 47) {
                    literalText2 = new LiteralText("Outdated server! I'm still on 1.8.9");
                    this.connection.send(new LoginDisconnectS2CPacket(literalText2));
                    this.connection.disconnect(literalText2);
                } else if (packet.getProtocolVersion() < 47) {
                    literalText2 = new LiteralText("Outdated client! Please use 1.8.9");
                    this.connection.send(new LoginDisconnectS2CPacket(literalText2));
                    this.connection.disconnect(literalText2);
                } else {
                    this.connection.setPacketListener(new ServerLoginNetworkHandler(this.server, this.connection));
                    if (TraverBric.useBungee) {
                        String[] split = packet.address.split("\00");
                        if (split.length == 3 || split.length == 4) {
                            packet.address = split[0];
                            connection.address = new java.net.InetSocketAddress(split[1], ((java.net.InetSocketAddress) connection.getAddress()).getPort());
                            ((BungeeConnectionExtra) connection).setSpoofedUUID(UUIDTypeAdapter.fromString(split[2]));
                        } else {
                            LiteralText disconnectText = new LiteralText("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                            this.connection.send(new DisconnectS2CPacket(disconnectText));
                            this.connection.disconnect(disconnectText);
                            return;
                        }
                        if (split.length == 4) {
                            ((BungeeConnectionExtra) connection).setSpoofedProfile(GSON.fromJson(split[3], Property[].class));
                        }
                    }
                }
                break;
            case STATUS:
                this.connection.setState(NetworkState.STATUS);
                this.connection.setPacketListener(new ServerQueryNetworkHandler(this.server, this.connection));
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packet.getIntendedState());
        }
    }
}
