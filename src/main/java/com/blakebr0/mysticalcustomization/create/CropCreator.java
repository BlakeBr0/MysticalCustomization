package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.cucumber.helper.ParsingHelper;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropModels;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.IdentifierException;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class CropCreator {
    public static Crop create(Identifier id, JsonObject json) throws JsonSyntaxException, IdentifierException {
        var ingredient = json.has("ingredient") ? GsonHelper.getAsJsonObject(json, "ingredient") : null;
        var material = LazyIngredient.EMPTY;

        if (ingredient != null) {
            if (ingredient.has("tag")) {
                var tag = GsonHelper.getAsString(ingredient, "tag");
                material = LazyIngredient.tag(tag);
            } else if (ingredient.has("item")) {
                var item = GsonHelper.getAsString(ingredient, "item");
                if (ingredient.has("components")) {
                    var components = DataComponentMap.CODEC.decode(JsonOps.INSTANCE, ingredient.get("components")).getOrThrow(JsonSyntaxException::new).getFirst();
                    material = LazyIngredient.item(item, components);
                } else {
                    material = LazyIngredient.item(item);
                }
            } else {
                throw new JsonSyntaxException("Ingredient must have either 'item' or 'tag' property");
            }
        }

        // crop tiers and types are assigned lazily in CropLoader
        var crop = new Crop(id, CropTier.ONE, CropType.RESOURCE, material);

        var tierId = GsonHelper.getAsString(json, "tier");
        var typeId = GsonHelper.getAsString(json, "type");

        CropLoader.CROP_TIER_MAP.put(crop, Identifier.parse(tierId));
        CropLoader.CROP_TYPE_MAP.put(crop, Identifier.parse(typeId));

        if (json.has("color")) {
            var color = GsonHelper.getAsString(json, "color");
            var i = ParsingHelper.parseHex(color, "color");
            crop.setColor(i);
        } else if (json.has("colors")) {
            var colors = GsonHelper.getAsJsonObject(json, "colors");
            if (colors.has("flower")) {
                var color = GsonHelper.getAsString(colors, "flower");
                var i = ParsingHelper.parseHex(color, "flower");

                crop.setFlowerColor(i);
            }

            if (colors.has("essence")) {
                var color = GsonHelper.getAsString(colors, "essence");
                var i = ParsingHelper.parseHex(color, "essence");

                crop.setEssenceColor(i);
            }

            if (colors.has("seeds")) {
                var color = GsonHelper.getAsString(colors, "seeds");
                var i = ParsingHelper.parseHex(color, "seeds");

                crop.setSeedColor(i);
            }
        }

        var ctextures = crop.getModels()
                .setFlowerModel(CropModels.FLOWER_INGOT_BLANK)
                .setEssenceModel(CropModels.ESSENCE_INGOT_BLANK)
                .setSeedModel(CropModels.SEED_BLANK);

        if (json.has("textures")) {
            var textures = GsonHelper.getAsJsonObject(json, "textures");
            if (textures.has("flower")) {
                var texture = GsonHelper.getAsString(textures, "flower");
                var location = Identifier.parse(texture);

                ctextures.setFlowerModel(location);
            }

            if (textures.has("essence")) {
                var texture = GsonHelper.getAsString(textures, "essence");
                var location = Identifier.parse(texture);

                ctextures.setEssenceModel(location);
            }

            if (textures.has("seeds")) {
                var texture = GsonHelper.getAsString(textures, "seeds");
                var location = Identifier.parse(texture);

                ctextures.setSeedModel(location);
            }
        }

        if (json.has("name")) {
            var name = GsonHelper.getAsString(json, "name");
            crop.setDisplayName(Component.literal(name));
        }

        if (json.has("baseSecondaryChance")) {
            var chance = GsonHelper.getAsDouble(json, "baseSecondaryChance");
            crop.setBaseSecondaryChance(chance);
        }

        if (json.has("respectsEffectiveFarmland")) {
            var respects = GsonHelper.getAsBoolean(json, "respectsEffectiveFarmland");
            crop.setRespectsEffectiveFarmland(respects);
        }

        if (json.has("enabled")) {
            var enabled = GsonHelper.getAsBoolean(json, "enabled");
            crop.setEnabled(enabled);
        }

        if (json.has("crux")) {
            var crux = GsonHelper.getAsString(json, "crux");
            CropLoader.CRUX_MAP.put(crop, Identifier.parse(crux));
        }

        if (json.has("glint")) {
            var glint = GsonHelper.getAsBoolean(json, "glint");
            crop.setHasEffect(glint);
        }

        if (json.has("biomes")) {
            var biomes = GsonHelper.getAsJsonArray(json, "biomes");

            biomes.forEach(biome -> {
                crop.addRequiredBiome(Identifier.parse(biome.getAsString()));
            });
        }

        // special case crops
        if (isGarbageSeed(crop.getName())) {
            DeferredHolder<Item, Item> essence = null;

            if ("insanium".equals(crop.getName())) {
                if (ModList.get().isLoaded("mysticalagradditions")) {
                    essence = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath("mysticalagradditions", crop.getNameWithSuffix("essence")));
                }
            } else {
                essence = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MysticalAgricultureAPI.MOD_ID, crop.getNameWithSuffix("essence")));
            }

            if (essence != null) {
                crop.setEssenceItem(essence);
            }
        }

        if (json.has("essence")) {
            var essence = GsonHelper.getAsString(json, "essence");
            var item =  DeferredHolder.create(Registries.ITEM, Identifier.parse(essence));

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

        return crop;
    }

    private static boolean isGarbageSeed(String name) {
        return "prudentium".equals(name)
                || "tertium".equals(name)
                || "imperium".equals(name)
                || "supremium".equals(name)
                || "awakened_supremium".equals(name)
                || "insanium".equals(name)
                || "fertilized".equals(name);
    }
}
