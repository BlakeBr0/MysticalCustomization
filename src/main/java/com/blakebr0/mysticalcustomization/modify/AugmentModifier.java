package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.GsonHelper;

public final class AugmentModifier {
    public static void modify(Augment augment, JsonObject json) throws JsonSyntaxException {
        if (json.has("enabled")) {
            var enabled = GsonHelper.getAsBoolean(json, "enabled");
            augment.setEnabled(enabled);
        }
    }
}
