package com.blakebr0.mysticalcustomization.lib;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.blakebr0.mysticalcustomization.loader.CropTierLoader;
import com.blakebr0.mysticalcustomization.loader.CropTypeLoader;
import com.blakebr0.mysticalcustomization.loader.MobSoulTypeLoader;

@MysticalAgriculturePlugin
public class ModCorePlugin implements IMysticalAgriculturePlugin {
    @Override
    public void onRegisterCrops(ICropRegistry registry) {
        CropTierLoader.onRegisterCrops();
        CropTypeLoader.onRegisterCrops();
        CropLoader.onRegisterCrops(registry);
    }

    @Override
    public void onPostRegisterCrops(ICropRegistry registry) {
        CropTierLoader.onPostRegisterCrops();
        CropTypeLoader.onPostRegisterCrops();
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
}
