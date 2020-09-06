package io.github.hydos.traverbric.mixin;

import com.google.common.collect.Iterators;
import com.mojang.authlib.Agent;
import com.mojang.authlib.ProfileLookupCallback;
import io.github.hydos.traverbric.TraverBric;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.util.ChatUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(ServerConfigHandler.class)
public class ServerConfigHandlerMixin {

    @Inject(method = "lookupProfile", at = @At(value = "HEAD"), cancellable = true)
    private static void lookupBungeeProfile(MinecraftServer server, Collection<String> bannedPlayers, ProfileLookupCallback callback, CallbackInfo ci) {
        String[] strings = Iterators.toArray(Iterators.filter(bannedPlayers.iterator(), string -> !ChatUtil.isEmpty(string)), String.class);
        if (server.isOnlineMode() || TraverBric.useBungee) {
            server.getGameProfileRepo().findProfilesByNames(strings, Agent.MINECRAFT, callback);
            ci.cancel();
        }
    }

}
