package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.*;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CropCreator {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);

    public static ICrop create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        String tierId = GsonHelper.getAsString(json, "tier");
        String typeId = GsonHelper.getAsString(json, "type");

        CropTier tier = MysticalAgricultureAPI.getCropTierById(new ResourceLocation(tierId));
        if (tier == null)
            throw new JsonSyntaxException("Invalid crop tier provided: " + tierId);

        CropType type = MysticalAgricultureAPI.getCropTypeByName(typeId);
        if (type == null)
            throw new JsonSyntaxException("Invalid crop type provided: " + typeId);

        JsonObject ingredient = json.has("ingredient") ? GsonHelper.getAsJsonObject(json, "ingredient") : null;
        LazyIngredient material = LazyIngredient.EMPTY;

        if (ingredient != null) {
            if (ingredient.has("tag")) {
                String tag = GsonHelper.getAsString(ingredient, "tag");
                material = LazyIngredient.tag(tag);
            } else if (ingredient.has("item")) {
                String item = GsonHelper.getAsString(ingredient, "item");
                if (ingredient.has("nbt")) {
                    CompoundTag nbt = ParsingUtils.parseNBT(ingredient.get("nbt"));
                    material = LazyIngredient.item(item, nbt);
                } else {
                    material = LazyIngredient.item(item);
                }
            } else {
                throw new JsonSyntaxException("Ingredient must have either 'item' or 'tag' property");
            }
        }

        Crop crop = new Crop(id, tier, type, material);

        if (json.has("color")) {
            String color = GsonHelper.getAsString(json, "color");
            int i = ParsingUtils.parseHex(color, "color");
            crop.setColor(i);
        } else if (json.has("colors")) {
            JsonObject colors = GsonHelper.getAsJsonObject(json, "colors");
            if (colors.has("flower")) {
                String color = GsonHelper.getAsString(colors, "flower");
                int i = ParsingUtils.parseHex(color, "flower");
                crop.setFlowerColor(i);
            }

            if (colors.has("essence")) {
                String color = GsonHelper.getAsString(colors, "essence");
                int i = ParsingUtils.parseHex(color, "essence");
                crop.setEssenceColor(i);
            }

            if (colors.has("seeds")) {
                String color = GsonHelper.getAsString(colors, "seeds");
                int i = ParsingUtils.parseHex(color, "seeds");
                crop.setSeedColor(i);
            }
        }

        CropTextures ctextures = crop.getTextures()
                .setFlowerTexture(CropTextures.FLOWER_INGOT_BLANK)
                .setEssenceTexture(CropTextures.ESSENCE_INGOT_BLANK)
                .setSeedTexture(CropTextures.SEED_BLANK);

        if (json.has("textures")) {
            JsonObject textures = GsonHelper.getAsJsonObject(json, "textures");
            if (textures.has("flower")) {
                String texture = GsonHelper.getAsString(textures, "flower");
                ResourceLocation location = new ResourceLocation(texture);
                ctextures.setFlowerTexture(location);
            }

            if (textures.has("essence")) {
                String texture = GsonHelper.getAsString(textures, "essence");
                ResourceLocation location = new ResourceLocation(texture);
                ctextures.setEssenceTexture(location);
            }

            if (textures.has("seeds")) {
                String texture = GsonHelper.getAsString(textures, "seeds");
                ResourceLocation location = new ResourceLocation(texture);
                ctextures.setSeedTexture(location);
            }
        }

        if (json.has("name")) {
            String name = GsonHelper.getAsString(json, "name");
            crop.setDisplayName(new TextComponent(name));
        }

        if (json.has("enabled")) {
            boolean enabled = GsonHelper.getAsBoolean(json, "enabled");
            crop.setEnabled(enabled);
        }

        if (json.has("crux")) {
            String crux = GsonHelper.getAsString(json, "crux");
            CropLoader.CRUX_MAP.put(crop, new ResourceLocation(crux));
        }

        if (json.has("glint")) {
            boolean glint = GsonHelper.getAsBoolean(json, "glint");
            crop.setHasEffect(glint);
        }

        if (json.has("biomes")) {
            JsonArray biomes = GsonHelper.getAsJsonArray(json, "biomes");

            biomes.forEach(biome -> {
                crop.addRequiredBiome(new ResourceLocation(biome.getAsString()));
            });
        }

        if (isGarbageSeed(crop.getName())) {
            RegistryObject<Item> essence = RegistryObject.of(new ResourceLocation(MysticalAgricultureAPI.MOD_ID, crop.getNameWithSuffix("essence")), ForgeRegistries.ITEMS);

            essence.updateReference(ForgeRegistries.ITEMS);

            if (essence.isPresent()) {
                crop.setEssence(essence);
            } else {
                LOGGER.error("Could not find the essence for crop {}", crop.getId());
            }
        }

        return crop;
    }

    private static boolean isGarbageSeed(String name) {
        return "prudentium".equals(name)
                || "tertium".equals(name)
                || "imperium".equals(name)
                || "supremium".equals(name)
                || "fertilized".equals(name);
    }
}
