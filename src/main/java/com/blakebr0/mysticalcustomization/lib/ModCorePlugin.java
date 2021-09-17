package com.blakebr0.mysticalcustomization.lib;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalcustomization.loader.AugmentLoader;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.blakebr0.mysticalcustomization.loader.CropTierLoader;
import com.blakebr0.mysticalcustomization.loader.CropTypeLoader;
import com.blakebr0.mysticalcustomization.loader.MobSoulTypeLoader;

@MysticalAgriculturePlugin
public final class ModCorePlugin implements IMysticalAgriculturePlugin {
    @Override
    public void onRegisterCrops(ICropRegistry registry) {
        CropTierLoader.onRegisterCrops(registry);
        CropTypeLoader.onRegisterCrops(registry);
        CropLoader.onRegisterCrops(registry);
    }

    @Override
    public void onPostRegisterCrops(ICropRegistry registry) {
        CropTierLoader.onPostRegisterCrops(registry);
        CropTypeLoader.onPostRegisterCrops(registry);
        CropLoader.onPostRegisterCrops(registry);
    }

    @Override
    public void onRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        MobSoulTypeLoader.onRegisterMobSoulTypes(registry);
    }

    @Override
    public void onPostRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        MobSoulTypeLoader.onPostRegisterMobSoulTypes(registry);
    }

    @Override
    public void onPostRegisterAugments(IAugmentRegistry registry) {
        AugmentLoader.onPostRegisterAugments(registry);
    }
}
