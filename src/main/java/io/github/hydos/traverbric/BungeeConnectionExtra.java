package io.github.hydos.traverbric;

import com.mojang.authlib.properties.Property;

import java.util.UUID;

public interface BungeeConnectionExtra {

    UUID getSpoofedUUID();

    void setSpoofedUUID(UUID fromString);

    Property[] getSpoofedProfile();

    void setSpoofedProfile(Property[] profile);
}
