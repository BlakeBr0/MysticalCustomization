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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public final class CropModifier {
    public static void modify(ICrop crop, JsonObject json) throws JsonSyntaxException {
        if (json.has("name")) {
            String name = JSONUtils.getAsString(json, "name");
            crop.setDisplayName(new StringTextComponent(name));
        }

        if (json.has("tier")) {
            String tierId = JSONUtils.getAsString(json, "tier");
            CropTier tier = MysticalAgricultureAPI.getCropTierById(new ResourceLocation(tierId));
            if (tier == null)
                throw new JsonSyntaxException("Invalid crop tier provided: " + tierId);

            crop.setTier(tier);
        }

        if (json.has("type")) {
            String typeId = JSONUtils.getAsString(json, "type");
            CropType type = MysticalAgricultureAPI.getCropTypeByName(typeId);
            if (type == null)
                throw new JsonSyntaxException("Invalid crop type provided: " + typeId);

            crop.setType(type);
        }

        if (json.has("ingredient")) {
            JsonObject ingredient = JSONUtils.getAsJsonObject(json, "ingredient");
            LazyIngredient material;
            if (ingredient.has("tag")) {
                String tag = JSONUtils.getAsString(ingredient, "tag");
                material = LazyIngredient.tag(tag);
            } else if (ingredient.has("item")) {
                String item = JSONUtils.getAsString(ingredient, "item");
                if (ingredient.has("nbt")) {
                    CompoundNBT nbt = ParsingUtils.parseNBT(ingredient.get("nbt"));
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
            boolean enabled = JSONUtils.getAsBoolean(json, "enabled");
            crop.setEnabled(enabled);
        }

        if (json.has("crux")) {
            String crux = JSONUtils.getAsString(json, "crux");
            CropLoader.CRUX_MAP.put(crop, new ResourceLocation(crux));
        }

        if (json.has("glint")) {
            boolean glint = JSONUtils.getAsBoolean(json, "glint");
            crop.setHasEffect(glint);
        }

        if (json.has("biomes")) {
            JsonArray biomes = JSONUtils.getAsJsonArray(json, "biomes");

            biomes.forEach(biome -> {
                crop.addRequiredBiome(new ResourceLocation(biome.getAsString()));
            });
        }
    }
}
