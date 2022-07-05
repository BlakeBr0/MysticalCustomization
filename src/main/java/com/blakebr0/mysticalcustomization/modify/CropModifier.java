package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CropModifier {
    public static void modify(Crop crop, JsonObject json) throws JsonSyntaxException {
        if (json.has("name")) {
            var name = GsonHelper.getAsString(json, "name");
            crop.setDisplayName(new TextComponent(name));
        }

        if (json.has("tier")) {
            var tierId = GsonHelper.getAsString(json, "tier");
            var tier = MysticalAgricultureAPI.getCropRegistry().getTierById(new ResourceLocation(tierId));
            if (tier == null)
                throw new JsonSyntaxException("Invalid crop tier provided: " + tierId);

            crop.setTier(tier);
        }

        if (json.has("type")) {
            var typeId = GsonHelper.getAsString(json, "type");
            var type = MysticalAgricultureAPI.getCropRegistry().getTypeById(new ResourceLocation(typeId));
            if (type == null)
                throw new JsonSyntaxException("Invalid crop type provided: " + typeId);

            crop.setType(type);
        }

        if (json.has("ingredient")) {
            var ingredient = GsonHelper.getAsJsonObject(json, "ingredient");
            LazyIngredient material;

            if (ingredient.has("tag")) {
                var tag = GsonHelper.getAsString(ingredient, "tag");
                material = LazyIngredient.tag(tag);
            } else if (ingredient.has("item")) {
                var item = GsonHelper.getAsString(ingredient, "item");
                if (ingredient.has("nbt")) {
                    var nbt = ParsingUtils.parseNBT(ingredient.get("nbt"));
                    material = LazyIngredient.item(item, nbt);
                } else {
                    material = LazyIngredient.item(item);
                }
            } else {
                throw new JsonSyntaxException("Ingredient must have either 'item' or 'tag' property");
            }

            crop.setCraftingMaterial(material);
        }

        if (json.has("baseSecondaryChance")) {
            var chance = GsonHelper.getAsDouble(json, "baseSecondaryChance");
            crop.setBaseSecondaryChance(chance);
        }

        if (json.has("enabled")) {
            var enabled = GsonHelper.getAsBoolean(json, "enabled");
            crop.setEnabled(enabled);
        }

        if (json.has("crux")) {
            if (json.get("crux").isJsonNull()) {
                CropLoader.CRUX_MAP.put(crop, null);
            } else {
                var crux = GsonHelper.getAsString(json, "crux");
                CropLoader.CRUX_MAP.put(crop, new ResourceLocation(crux));
            }
        }

        if (json.has("glint")) {
            var glint = GsonHelper.getAsBoolean(json, "glint");
            crop.setHasEffect(glint);
        }

        if (json.has("biomes")) {
            var biomes = GsonHelper.getAsJsonArray(json, "biomes");

            biomes.forEach(biome -> {
                crop.addRequiredBiome(new ResourceLocation(biome.getAsString()));
            });
        }

        if (json.has("essence")) {
            var essence = GsonHelper.getAsString(json, "essence");
            var item = RegistryObject.create(new ResourceLocation(essence), ForgeRegistries.ITEMS);

            crop.setEssenceItem(item);
        }

        if (json.has("recipes")) {
            var recipes = GsonHelper.getAsJsonObject(json, "recipes");
            var config = crop.getRecipeConfig();

            if (recipes.has("crafting")) {
                var enabled = GsonHelper.getAsBoolean(recipes, "crafting");
                config.setSeedCraftingRecipeEnabled(enabled);
            }

            if (recipes.has("infusion")) {
                var enabled = GsonHelper.getAsBoolean(recipes, "infusion");
                config.setSeedInfusionRecipeEnabled(enabled);
            }

            if (recipes.has("reprocessor")) {
                var enabled = GsonHelper.getAsBoolean(recipes, "reprocessor");
                config.setSeedReprocessorRecipeEnabled(enabled);
            }
        }
    }
}
