package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class MobSoulTypeLoader {
    public static void onRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        Path path = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/mobsoultypes/");
    }

    public static void onPostRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {

    }
}
