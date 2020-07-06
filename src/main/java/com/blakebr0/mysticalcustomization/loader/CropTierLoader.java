package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.CropTierCreator;
import com.blakebr0.mysticalcustomization.modify.CropTierModifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public final class CropTierLoader {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<CropTier, ResourceLocation> FARMLAND_MAP = new HashMap<>();
    public static final Map<CropTier, ResourceLocation> ESSENCE_MAP = new HashMap<>();

    public static void onRegisterCrops() {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/tiers/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/tiers/ directory");
        }

        File[] files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (File file : files) {
            JsonObject json;
            FileReader reader = null;
            ResourceLocation id = null;

            try {
                JsonParser parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();
                String name = file.getName().replace(".json", "");
                id = new ResourceLocation(MysticalCustomization.MOD_ID, name);
                CropTierCreator.create(id, json);

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while creating crop tier with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    public static void onPostRegisterCrops() {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        File file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-tiers.json").toFile();
        if (file.exists() && file.isFile()) {
            JsonObject json;
            FileReader reader = null;
            try {
                JsonParser parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();

                json.entrySet().forEach(entry -> {
                    String id = entry.getKey();
                    JsonObject changes = entry.getValue().getAsJsonObject();
                    CropTier tier = MysticalAgricultureAPI.getCropTierById(new ResourceLocation(id));

                    CropTierModifier.modify(tier, changes);
                });

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while reading configure-tiers.json", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (Writer writer = new FileWriter(file)) {
                JsonObject object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                LOGGER.error("An error occurred while creating configure-tiers.json", e);
            }
        }
    }

    public static void onCommonSetup() {
        FARMLAND_MAP.forEach((tier, block) -> {
            Block farmland = ForgeRegistries.BLOCKS.getValue(block);
            if (farmland instanceof FarmlandBlock) {
                tier.setFarmland(() -> (FarmlandBlock) farmland);
            } else {
                LOGGER.error("Invalid farmland block provided");
            }
        });

        ESSENCE_MAP.forEach((tier, item) -> {
            Item essence = ForgeRegistries.ITEMS.getValue(item);
            if (essence != Items.AIR) {
                tier.setEssence(() -> essence);
            } else {
                throw new JsonSyntaxException("Invalid essence item provided");
            }
        });
    }
}
