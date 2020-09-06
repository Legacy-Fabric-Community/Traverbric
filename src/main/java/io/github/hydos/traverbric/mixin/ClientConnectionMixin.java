package io.github.hydos.traverbric.mixin;

import com.mojang.authlib.properties.Property;
import io.github.hydos.traverbric.BungeeConnectionExtra;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements BungeeConnectionExtra {

    public UUID spoofedUUID;
    public Property[] spoofedProfile;

    @Override
    public UUID getSpoofedUUID() {
        return spoofedUUID;
    }

    @Override
    public void setSpoofedUUID(UUID spoofedUUID) {
        this.spoofedUUID = spoofedUUID;
    }

    @Override
    public Property[] getSpoofedProfile() {
        return spoofedProfile;
    }

    @Override
    public void setSpoofedProfile(Property[] profile) {
        spoofedProfile = profile;
    }
}
