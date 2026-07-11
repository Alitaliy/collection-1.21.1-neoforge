package com.collection.client.gui;

import com.collection.collectible.CollectibleCatalog;
import com.collection.collectible.CollectibleDefinition;
import com.collection.collectible.CollectibleSetDefinition;
import com.collection.progress.ModAttachments;
import com.collection.progress.PlayerCollectionProgress;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.locale.Language;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CollectorJournalScreen extends Screen {
    private static final int JOURNAL_WIDTH = 320;
    private static final int JOURNAL_HEIGHT = 196;
    private static final int NAV_WIDTH = 86;
    private static final int INNER_PADDING = 10;
    private static final int CONTENT_X = NAV_WIDTH + 16;
    private static final int CONTENT_WIDTH = JOURNAL_WIDTH - CONTENT_X - INNER_PADDING;
    private static final int CONTENT_HEIGHT = JOURNAL_HEIGHT - 34;
    private static final int COLOR_SHADOW = 0x90150E09;
    private static final int COLOR_COVER = 0xFF4B3326;
    private static final int COLOR_PAGE = 0xFFF5ECD8;
    private static final int COLOR_PAGE_DARK = 0xFFE3D4BA;
    private static final int COLOR_NAV = 0xFFF0E2C8;
    private static final int COLOR_NAV_ACTIVE = 0xFFD8C19A;
    private static final int COLOR_NAV_HOVER = 0xFFE6D2AF;
    private static final int COLOR_CARD = 0xFFF9F2E5;
    private static final int COLOR_BORDER = 0xFF8B6A48;
    private static final int COLOR_TEXT = 0xFF3C2C21;
    private static final int COLOR_SUBTEXT = 0xFF6F573F;
    private static final int COLOR_GOOD = 0xFF2E6A34;
    private static final int COLOR_WARN = 0xFF8A6B2F;

    private final List<Button> navigationButtons = new ArrayList<>();
    private int pageIndex;

    public CollectorJournalScreen() {
        super(Component.translatable("collection.journal.summary_header"));
    }

    @Override
    protected void init() {
        this.navigationButtons.clear();
        this.buildNavigation();
    }

    private void buildNavigation() {
        int left = this.pageLeft();
        int top = this.pageTop();
        int buttonX = left + 8;
        int buttonWidth = NAV_WIDTH - 12;
        int buttonY = top + 28;
        int buttonHeight = 20;
        int gap = 4;

        this.addNavigationButton(buttonX, buttonY, buttonWidth, buttonHeight, Component.translatable("collection.journal.screen.summary"), 0);
        buttonY += buttonHeight + gap;

        for (int index = 0; index < CollectibleCatalog.SETS.size(); index++) {
            this.addNavigationButton(buttonX, buttonY, buttonWidth, buttonHeight, CollectibleCatalog.SETS.get(index).name(), index + 1);
            buttonY += buttonHeight + gap;
        }

        this.updateButtonState();
    }

    private void addNavigationButton(int x, int y, int width, int height, Component label, int targetPage) {
        Button button = this.addRenderableWidget(new JournalNavButton(x, y, width, height, label, targetPage));
        this.navigationButtons.add(button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = this.pageLeft();
        int top = this.pageTop();
        Player player = this.minecraft != null ? this.minecraft.player : null;

        this.renderJournalFrame(guiGraphics, left, top);

        if (player == null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("collection.journal.screen.empty"), left + JOURNAL_WIDTH / 2, top + 92, COLOR_TEXT);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        PlayerCollectionProgress progress = player.getData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
        guiGraphics.drawCenteredString(this.font, this.title, left + JOURNAL_WIDTH / 2, top + 12, COLOR_TEXT);
        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("collection.journal.screen.page", this.pageIndex + 1, this.pageCount()),
                left + JOURNAL_WIDTH / 2,
                top + JOURNAL_HEIGHT - 16,
                COLOR_SUBTEXT
        );

        if (this.pageIndex == 0) {
            this.renderSummaryPage(guiGraphics, left, top, mouseX, mouseY, progress);
        } else {
            CollectibleSetDefinition set = CollectibleCatalog.SETS.get(this.pageIndex - 1);
            this.renderSetPage(guiGraphics, left, top, mouseX, mouseY, progress, set);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {
    }

    private void renderJournalFrame(GuiGraphics guiGraphics, int left, int top) {
        int right = left + JOURNAL_WIDTH;
        int bottom = top + JOURNAL_HEIGHT;

        guiGraphics.fill(left - 6, top - 6, right + 6, bottom + 6, COLOR_SHADOW);
        guiGraphics.fill(left, top, right, bottom, COLOR_COVER);
        guiGraphics.fill(left + 4, top + 4, right - 4, bottom - 4, COLOR_PAGE_DARK);
        guiGraphics.fill(left + 8, top + 8, left + NAV_WIDTH, bottom - 8, COLOR_NAV);
        guiGraphics.fill(left + CONTENT_X - 6, top + 8, right - 8, bottom - 8, COLOR_PAGE);
        guiGraphics.fill(left + CONTENT_X - 8, top + 8, left + CONTENT_X - 6, bottom - 8, COLOR_BORDER);
        guiGraphics.fill(left + 8, top + 24, right - 8, top + 25, COLOR_BORDER);
    }

    private void renderSummaryPage(
            GuiGraphics guiGraphics,
            int left,
            int top,
            int mouseX,
            int mouseY,
            PlayerCollectionProgress progress
    ) {
        int contentLeft = left + CONTENT_X;
        int contentTop = top + 34;
        int contentRight = left + JOURNAL_WIDTH - INNER_PADDING;
        long dayTime = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getDayTime() : 0L;
        CollectibleSetDefinition featuredSet = CollectibleCatalog.featuredSetForDay(dayTime);

        guiGraphics.drawString(this.font, Component.translatable("collection.journal.screen.summary"), contentLeft, contentTop, COLOR_TEXT, false);
        guiGraphics.drawString(
                this.font,
                Component.translatable("collection.journal.screen.total", progress.discoveredTotal(), CollectibleCatalog.COLLECTIBLES.size()),
                contentLeft,
                contentTop + 14,
                COLOR_TEXT,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable("collection.journal.screen.featured", featuredSet.name()),
                contentLeft,
                contentTop + 28,
                COLOR_SUBTEXT,
                false
        );
        this.drawWrappedLines(
                guiGraphics,
                Component.translatable("collection.journal.screen.featured_bonus"),
                contentLeft,
                contentTop + 42,
                CONTENT_WIDTH - 8,
                2,
                COLOR_SUBTEXT
        );

        int listTop = contentTop + 70;
        int rowHeight = 20;
        for (int index = 0; index < CollectibleCatalog.SETS.size(); index++) {
            CollectibleSetDefinition set = CollectibleCatalog.SETS.get(index);
            int rowY = listTop + index * (rowHeight + 4);
            int discovered = progress.discoveredCount(set);
            boolean claimed = progress.hasClaimedReward(set.id());

            guiGraphics.fill(contentLeft - 4, rowY - 3, contentRight - 4, rowY + rowHeight - 1, COLOR_CARD);
            guiGraphics.fill(contentLeft - 4, rowY - 3, contentRight - 4, rowY - 2, COLOR_PAGE_DARK);
            guiGraphics.renderFakeItem(set.createRewardStack(), contentLeft, rowY);
            guiGraphics.drawString(this.font, set.name(), contentLeft + 22, rowY + 4, COLOR_TEXT, false);

            String progressText = discovered + "/" + set.size() + (claimed ? " [OK]" : "");
            int progressWidth = this.font.width(progressText);
            guiGraphics.drawString(
                    this.font,
                    Component.literal(progressText),
                    contentRight - 10 - progressWidth,
                    rowY + 4,
                    claimed ? COLOR_GOOD : COLOR_SUBTEXT,
                    false
            );

            if (mouseX >= contentLeft && mouseX <= contentLeft + 16 && mouseY >= rowY && mouseY <= rowY + 16) {
                guiGraphics.renderTooltip(this.font, set.createRewardStack(), mouseX, mouseY);
            }
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
        int contentLeft = left + CONTENT_X;
        int contentTop = top + 34;
        int contentRight = left + JOURNAL_WIDTH - INNER_PADDING;
        ItemStack rewardStack = set.createRewardStack();
        boolean complete = progress.isSetComplete(set);
        boolean claimed = progress.hasClaimedReward(set.id());
        long dayTime = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getDayTime() : 0L;
        boolean featured = CollectibleCatalog.featuredSetForDay(dayTime).id().equals(set.id());

        guiGraphics.drawString(this.font, set.name(), contentLeft, contentTop, COLOR_TEXT, false);
        guiGraphics.renderFakeItem(rewardStack, contentRight - 26, contentTop - 2);
        guiGraphics.drawString(
                this.font,
                claimed
                        ? Component.translatable("collection.journal.screen.reward_claimed", rewardStack.getHoverName())
                        : Component.translatable("collection.journal.screen.reward_missing"),
                contentLeft,
                contentTop + 14,
                claimed ? COLOR_GOOD : COLOR_SUBTEXT,
                false
        );
        guiGraphics.drawString(
                this.font,
                complete
                        ? Component.translatable("collection.journal.screen.complete")
                        : Component.translatable("collection.journal.screen.incomplete"),
                contentLeft,
                contentTop + 28,
                complete ? COLOR_GOOD : COLOR_WARN,
                false
        );
        if (featured) {
            guiGraphics.drawString(this.font, Component.translatable("collection.journal.screen.featured_page"), contentLeft + 120, contentTop + 28, COLOR_WARN, false);
        }

        int cardTop = contentTop + 46;
        int cardHeight = 30;
        List<CollectibleDefinition> collectibles = set.collectibles();
        guiGraphics.enableScissor(contentLeft - 6, cardTop - 6, contentRight - 4, top + JOURNAL_HEIGHT - 10);
        for (int index = 0; index < collectibles.size(); index++) {
            CollectibleDefinition collectible = collectibles.get(index);
            ItemStack stack = collectible.item().get().getDefaultInstance();
            boolean found = progress.hasDiscovered(collectible.id());
            int cardY = cardTop + index * (cardHeight + 4);

            guiGraphics.fill(contentLeft - 4, cardY - 4, contentRight - 4, cardY + cardHeight, COLOR_CARD);
            guiGraphics.fill(contentLeft - 4, cardY - 4, contentRight - 4, cardY - 3, COLOR_PAGE_DARK);

            int iconX = contentLeft;
            int iconY = cardY + 6;
            guiGraphics.renderFakeItem(stack, iconX, iconY);
            guiGraphics.drawString(this.font, collectible.name(), iconX + 22, cardY + 2, COLOR_TEXT, false);
            guiGraphics.drawString(
                    this.font,
                    found ? Component.translatable("collection.journal.item_found", collectible.name()) : Component.translatable("collection.journal.item_missing", collectible.name()),
                    iconX + 22,
                    cardY + 14,
                    found ? COLOR_GOOD : COLOR_SUBTEXT,
                    false
            );

            if (!found) {
                this.drawWrappedLines(guiGraphics, collectible.clue(), iconX + 22, cardY + 23, CONTENT_WIDTH - 40, 1, COLOR_SUBTEXT);
            }
        }
        guiGraphics.disableScissor();

        if (mouseX >= contentRight - 26 && mouseX <= contentRight - 10 && mouseY >= contentTop - 2 && mouseY <= contentTop + 14) {
            guiGraphics.renderTooltip(this.font, rewardStack, mouseX, mouseY);
        }
        for (int index = 0; index < collectibles.size(); index++) {
            int iconX = contentLeft;
            int iconY = cardTop + index * (cardHeight + 4) + 6;
            if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                ItemStack stack = collectibles.get(index).item().get().getDefaultInstance();
                guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }
        }
    }

    private void drawWrappedLines(
            GuiGraphics guiGraphics,
            Component component,
            int x,
            int y,
            int width,
            int maxLines,
            int color
    ) {
        List<FormattedCharSequence> lines = this.font.split(component, width);
        int lineCount = Math.min(lines.size(), maxLines);
        for (int index = 0; index < lineCount; index++) {
            guiGraphics.drawString(this.font, lines.get(index), x, y + index * 10, color, false);
        }
    }

    private void openPage(int targetPage) {
        this.pageIndex = Mth.clamp(targetPage, 0, this.pageCount() - 1);
        this.updateButtonState();
    }

    private void updateButtonState() {
        for (int index = 0; index < this.navigationButtons.size(); index++) {
            Button button = this.navigationButtons.get(index);
            boolean active = index != this.pageIndex;
            button.active = active;
            button.visible = true;
            button.setAlpha(active ? 1.0F : 0.85F);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int pageCount() {
        return 1 + CollectibleCatalog.SETS.size();
    }

    private int pageLeft() {
        return (this.width - JOURNAL_WIDTH) / 2;
    }

    private int pageTop() {
        return (this.height - JOURNAL_HEIGHT) / 2;
    }

    private final class JournalNavButton extends Button {
        private final int targetPage;

        private JournalNavButton(int x, int y, int width, int height, Component message, int targetPage) {
            super(x, y, width, height, message, ignored -> CollectorJournalScreen.this.openPage(targetPage), DEFAULT_NARRATION);
            this.targetPage = targetPage;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = CollectorJournalScreen.this.pageIndex == this.targetPage;
            int backgroundColor = selected ? COLOR_NAV_ACTIVE : (this.isHovered() ? COLOR_NAV_HOVER : COLOR_NAV);
            int left = this.getX();
            int top = this.getY();
            int right = left + this.width;
            int bottom = top + this.height;

            guiGraphics.fill(left, top, right, bottom, backgroundColor);
            guiGraphics.fill(left, top, right, top + 1, COLOR_BORDER);
            guiGraphics.fill(left, bottom - 1, right, bottom, COLOR_BORDER);
            guiGraphics.fill(left, top, left + 1, bottom, COLOR_BORDER);
            guiGraphics.fill(right - 1, top, right, bottom, COLOR_BORDER);

            if (selected) {
                guiGraphics.fill(right - 2, top + 2, right, bottom - 2, COLOR_PAGE);
            }

            FormattedText trimmedText = CollectorJournalScreen.this.font.substrByWidth(this.getMessage(), this.width - 12);
            FormattedCharSequence trimmed = Language.getInstance().getVisualOrder(trimmedText);
            int textWidth = CollectorJournalScreen.this.font.width(trimmed);
            int textX = left + 6;
            int textY = top + (this.height - 8) / 2;
            if (textWidth < this.width - 12) {
                textX = left + (this.width - textWidth) / 2;
            }

            guiGraphics.drawString(CollectorJournalScreen.this.font, trimmed, textX, textY, COLOR_TEXT, false);
        }
    }
}
