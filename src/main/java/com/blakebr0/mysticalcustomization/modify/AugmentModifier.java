package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.tinkering.IAugment;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;

public final class AugmentModifier {
    public static void modify(IAugment augment, JsonObject json) throws JsonSyntaxException {
        if (json.has("enabled")) {
            boolean enabled = JSONUtils.getAsBoolean(json, "enabled");
            augment.setEnabled(enabled);
        }
    }
}
