package com.collection;

import com.collection.client.gui.CollectorJournalScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Collection.MODID, dist = Dist.CLIENT)
public final class CollectionClient {
    public CollectionClient() {
    }

    public static void openJournalScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.setScreen(new CollectorJournalScreen());
        }
    }
}
