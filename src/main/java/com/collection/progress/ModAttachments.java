package com.collection.progress;

import com.collection.Collection;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Collection.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerCollectionProgress>> PLAYER_COLLECTION_PROGRESS =
            ATTACHMENTS.register("player_collection_progress", () ->
                    AttachmentType.serializable(PlayerCollectionProgress::new)
                            .copyOnDeath()
                            .build()
            );

    private ModAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}
