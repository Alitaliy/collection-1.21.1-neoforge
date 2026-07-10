package com.collection.client.gui;

import com.collection.collectible.CollectibleCatalog;
import com.collection.collectible.CollectibleDefinition;
import com.collection.collectible.CollectibleSetDefinition;
import com.collection.progress.ModAttachments;
import com.collection.progress.PlayerCollectionProgress;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CollectorJournalScreen extends Screen {
    private static final int PAGE_WIDTH = 248;
    private static final int PAGE_HEIGHT = 176;

    private int pageIndex;
    private Button previousButton;
    private Button nextButton;

    public CollectorJournalScreen() {
        super(Component.translatable("collection.journal.summary_header"));
    }

    @Override
    protected void init() {
        int left = this.pageLeft();
        int top = this.pageTop();

        this.previousButton = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.changePage(-1))
                .bounds(left + 14, top + PAGE_HEIGHT - 24, 20, 20)
                .build());
        this.nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.changePage(1))
                .bounds(left + PAGE_WIDTH - 34, top + PAGE_HEIGHT - 24, 20, 20)
                .build());
        this.updateButtonState();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int left = this.pageLeft();
        int top = this.pageTop();
        Player player = this.minecraft != null ? this.minecraft.player : null;

        guiGraphics.fillGradient(left - 4, top - 4, left + PAGE_WIDTH + 4, top + PAGE_HEIGHT + 4, 0xCC23150F, 0xCC130B07);
        guiGraphics.fill(left, top, left + PAGE_WIDTH, top + PAGE_HEIGHT, 0xF3E9D4);
        guiGraphics.fill(left + 8, top + 22, left + PAGE_WIDTH - 8, top + PAGE_HEIGHT - 32, 0x33AA8B5D);

        if (player == null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("collection.journal.screen.empty"), left + PAGE_WIDTH / 2, top + 80, 0x4A3425);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        PlayerCollectionProgress progress = player.getData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
        guiGraphics.drawCenteredString(this.font, this.title, left + PAGE_WIDTH / 2, top + 10, 0x4A3425);
        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("collection.journal.screen.page", this.pageIndex + 1, this.pageCount()),
                left + PAGE_WIDTH / 2,
                top + PAGE_HEIGHT - 18,
                0x6B4F3D
        );

        if (this.pageIndex == 0) {
            this.renderSummaryPage(guiGraphics, left, top, progress);
        } else {
            CollectibleSetDefinition set = CollectibleCatalog.SETS.get(this.pageIndex - 1);
            this.renderSetPage(guiGraphics, left, top, mouseX, mouseY, progress, set);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderSummaryPage(GuiGraphics guiGraphics, int left, int top, PlayerCollectionProgress progress) {
        long dayTime = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getDayTime() : 0L;
        CollectibleSetDefinition featuredSet = CollectibleCatalog.featuredSetForDay(dayTime);

        guiGraphics.drawString(this.font, Component.translatable("collection.journal.screen.summary"), left + 16, top + 32, 0x4A3425, false);
        guiGraphics.drawString(
                this.font,
                Component.translatable("collection.journal.screen.total", progress.discoveredTotal(), CollectibleCatalog.COLLECTIBLES.size()),
                left + 16,
                top + 46,
                0x4A3425,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable("collection.journal.screen.featured", featuredSet.name()),
                left + 16,
                top + 58,
                0x6B4F3D,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable("collection.journal.screen.featured_bonus"),
                left + 16,
                top + 70,
                0x6B4F3D,
                false
        );

        int rowY = top + 88;
        for (CollectibleSetDefinition set : CollectibleCatalog.SETS) {
            int discovered = progress.discoveredCount(set);
            guiGraphics.renderFakeItem(set.createRewardStack(), left + 16, rowY - 4);
            guiGraphics.drawString(this.font, set.name(), left + 38, rowY, 0x4A3425, false);
            guiGraphics.drawString(
                    this.font,
                    Component.literal(discovered + "/" + set.size() + (progress.hasClaimedReward(set.id()) ? " ✓" : "")),
                    left + PAGE_WIDTH - 48,
                    rowY,
                    progress.hasClaimedReward(set.id()) ? 0x2E6A34 : 0x6B4F3D,
                    false
            );

            rowY += 16;
        }
    }

    private void renderSetPage(
            GuiGraphics guiGraphics,
            int left,
            int top,
            int mouseX,
            int mouseY,
            PlayerCollectionProgress progress,
            CollectibleSetDefinition set
    ) {
        guiGraphics.drawString(this.font, set.name(), left + 16, top + 32, 0x4A3425, false);
        guiGraphics.renderFakeItem(set.createRewardStack(), left + PAGE_WIDTH - 34, top + 28);

        int rowY = top + 58;
        List<CollectibleDefinition> collectibles = set.collectibles();
        for (int index = 0; index < collectibles.size(); index++) {
            CollectibleDefinition collectible = collectibles.get(index);
            ItemStack stack = collectible.item().get().getDefaultInstance();
            boolean found = progress.hasDiscovered(collectible.id());
            int iconX = left + 16;
            int iconY = rowY + index * 34;

            guiGraphics.renderFakeItem(stack, iconX, iconY);
            guiGraphics.drawString(this.font, collectible.name(), iconX + 24, iconY + 1, 0x4A3425, false);
            guiGraphics.drawString(
                    this.font,
                    found
                            ? Component.translatable("collection.journal.item_found", collectible.name()).withStyle(ChatFormatting.DARK_GREEN)
                            : Component.translatable("collection.journal.item_missing", collectible.name()).withStyle(ChatFormatting.DARK_RED),
                    iconX + 24,
                    iconY + 13,
                    0x4A3425,
                    false
            );

            if (!found) {
                guiGraphics.drawWordWrap(this.font, Component.translatable("collection.journal.clue", collectible.clue()), iconX + 24, iconY + 24, 188, 0x6B4F3D);
            }

            if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }
        }

        long dayTime = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getDayTime() : 0L;
        if (CollectibleCatalog.featuredSetForDay(dayTime).id().equals(set.id())) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("collection.journal.screen.featured_page"), left + PAGE_WIDTH / 2, top + PAGE_HEIGHT - 56, 0x8A6B2F);
        }
        Component status = progress.isSetComplete(set)
                ? Component.translatable("collection.journal.screen.complete").withStyle(ChatFormatting.DARK_GREEN)
                : Component.translatable("collection.journal.screen.incomplete").withStyle(ChatFormatting.GOLD);
        guiGraphics.drawCenteredString(this.font, status, left + PAGE_WIDTH / 2, top + PAGE_HEIGHT - 44, 0x4A3425);
        guiGraphics.drawCenteredString(this.font, Component.translatable("collection.journal.screen.map_hint"), left + PAGE_WIDTH / 2, top + PAGE_HEIGHT - 32, 0x6B4F3D);
    }

    private void changePage(int delta) {
        this.pageIndex = Mth.clamp(this.pageIndex + delta, 0, this.pageCount() - 1);
        this.updateButtonState();
    }

    private void updateButtonState() {
        if (this.previousButton != null) {
            this.previousButton.active = this.pageIndex > 0;
        }
        if (this.nextButton != null) {
            this.nextButton.active = this.pageIndex < this.pageCount() - 1;
        }
    }

    private int pageCount() {
        return 1 + CollectibleCatalog.SETS.size();
    }

    private int pageLeft() {
        return (this.width - PAGE_WIDTH) / 2;
    }

    private int pageTop() {
        return (this.height - PAGE_HEIGHT) / 2;
    }
}
