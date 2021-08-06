package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.crop.ICrop;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;

public final class CropModifier {
    public static void modify(ICrop crop, JsonObject json) throws JsonSyntaxException {
        if (json.has("name")) {
            String name = GsonHelper.getAsString(json, "name");
            crop.setDisplayName(new TextComponent(name));
        }

        if (json.has("tier")) {
            String tierId = GsonHelper.getAsString(json, "tier");
            CropTier tier = MysticalAgricultureAPI.getCropTierById(new ResourceLocation(tierId));
            if (tier == null)
                throw new JsonSyntaxException("Invalid crop tier provided: " + tierId);

            crop.setTier(tier);
        }

        if (json.has("type")) {
            String typeId = GsonHelper.getAsString(json, "type");
            CropType type = MysticalAgricultureAPI.getCropTypeByName(typeId);
            if (type == null)
                throw new JsonSyntaxException("Invalid crop type provided: " + typeId);

            crop.setType(type);
        }

        if (json.has("ingredient")) {
            JsonObject ingredient = GsonHelper.getAsJsonObject(json, "ingredient");
            LazyIngredient material;
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

            crop.setCraftingMaterial(material);
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
    }
}
