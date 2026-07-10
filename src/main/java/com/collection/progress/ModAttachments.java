package com.collection.progress;

import com.collection.Collection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Collection.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerCollectionProgress>> PLAYER_COLLECTION_PROGRESS =
            ATTACHMENTS.register("player_collection_progress", () ->
                    AttachmentType.serializable(PlayerCollectionProgress::new)
                            .copyOnDeath()
                            .sync(new AttachmentSyncHandler<>() {
                                @Override
                                public boolean sendToPlayer(net.neoforged.neoforge.attachment.IAttachmentHolder holder, net.minecraft.server.level.ServerPlayer to) {
                                    return holder == to;
                                }

                                @Override
                                public void write(RegistryFriendlyByteBuf buf, PlayerCollectionProgress attachment, boolean initialSync) {
                                    buf.writeNbt(attachment.serializeNBT(buf.registryAccess()));
                                }

                                @Nullable
                                @Override
                                public PlayerCollectionProgress read(
                                        net.neoforged.neoforge.attachment.IAttachmentHolder holder,
                                        RegistryFriendlyByteBuf buf,
                                        @Nullable PlayerCollectionProgress previousValue
                                ) {
                                    CompoundTag tag = buf.readNbt();
                                    PlayerCollectionProgress progress = previousValue != null ? previousValue : new PlayerCollectionProgress();
                                    progress.deserializeNBT(buf.registryAccess(), tag != null ? tag : new CompoundTag());
                                    return progress;
                                }
                            })
                            .build()
            );

    private ModAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}
