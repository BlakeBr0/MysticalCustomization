package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.crop.ICrop;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.CropCreator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class CropLoader {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);
    public static final Map<ICrop, ResourceLocation> CRUX_MAP = new HashMap<>();

    public static void onRegisterCrops(ICropRegistry registry) {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/crops/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/crops/ directory");
        }

        File[] files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (File file : files) {
            JsonObject json;
            FileReader reader = null;
            ResourceLocation id = null;
            ICrop crop = null;

            try {
                JsonParser parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();
                String name = file.getName().replace(".json", "");
                id = new ResourceLocation(MysticalCustomization.MOD_ID, name);
                crop = CropCreator.create(id, json);

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while creating crop with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (crop != null)
                registry.register(crop);
        }
    }

    public static void onPostRegisterCrops(ICropRegistry registry) {

    }

    public static void onCommonSetup() {
        CRUX_MAP.forEach((crop, crux) -> {
            Block block = ForgeRegistries.BLOCKS.getValue(crux);
            if (block != Blocks.AIR) {
                crop.setCrux(() -> block);
            } else {
                LOGGER.error("Could not find crux for crop {}", crop.getId());
            }
        });
    }
}
