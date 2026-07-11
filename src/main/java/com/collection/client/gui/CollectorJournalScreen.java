package com.collection.client.gui;

import com.collection.collectible.CollectibleCatalog;
import com.collection.collectible.CollectibleDefinition;
import com.collection.collectible.CollectibleSetDefinition;
import com.collection.progress.ModAttachments;
import com.collection.progress.PlayerCollectionProgress;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CollectorJournalScreen extends Screen {
    private static final int BOOK_WIDTH = 396;
    private static final int BOOK_HEIGHT = 224;
    private static final int LEFT_PAGE_X = 24;
    private static final int RIGHT_PAGE_X = 218;
    private static final int PAGE_TOP = 18;
    private static final int PAGE_WIDTH = 150;
    private static final int PAGE_HEIGHT = 184;
    private static final int LINE_HEIGHT = 11;
    private static final int COLOR_SHADOW = 0xAA000000;
    private static final int COLOR_COVER = 0xFF151419;
    private static final int COLOR_COVER_EDGE = 0xFF2C2017;
    private static final int COLOR_PAGE = 0xFFFFF8E4;
    private static final int COLOR_PAGE_SHADE = 0xFFE8DDBF;
    private static final int COLOR_PAGE_LINE = 0xFF9B927C;
    private static final int COLOR_TEXT = 0xFF3A3027;
    private static final int COLOR_MUTED = 0xFF6C5E4B;
    private static final int COLOR_HOVER = 0xFFE7D9B4;
    private static final int COLOR_PROGRESS_BACK = 0xFFC7C7C7;
    private static final int COLOR_PROGRESS_FILL = 0xFFE6F23B;
    private static final int COLOR_PROGRESS_FRAME = 0xFF3C342B;
    private static final int COLOR_GOOD = 0xFF2E6A34;
    private static final int COLOR_WARN = 0xFF8A6B2F;

    private int pageIndex;

    public CollectorJournalScreen() {
        super(Component.translatable("collection.journal.summary_header"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = this.pageLeft();
        int top = this.pageTop();
        Player player = this.minecraft != null ? this.minecraft.player : null;

        this.renderBook(guiGraphics, left, top);

        if (player == null) {
            this.drawPageTitle(guiGraphics, left + LEFT_PAGE_X, top + PAGE_TOP, Component.translatable("collection.journal.summary_header"));
            this.drawWrappedLines(guiGraphics, Component.translatable("collection.journal.screen.empty"), left + LEFT_PAGE_X + 6, top + PAGE_TOP + 30, PAGE_WIDTH - 12, 5, COLOR_TEXT);
            return;
        }

        PlayerCollectionProgress progress = player.getData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
        if (this.pageIndex == 0) {
            this.renderIndex(guiGraphics, left, top, mouseX, mouseY, progress);
        } else {
            CollectibleSetDefinition set = CollectibleCatalog.SETS.get(this.pageIndex - 1);
            this.renderSet(guiGraphics, left, top, mouseX, mouseY, progress, set);
        }

        this.renderPageArrows(guiGraphics, left, top, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int left = this.pageLeft();
        int top = this.pageTop();
        int previousX = left + 172;
        int nextX = left + BOOK_WIDTH - 28;
        int arrowY = top + BOOK_HEIGHT - 20;

        if (this.isInside(mouseX, mouseY, previousX, arrowY, 18, 14)) {
            this.changePage(-1);
            return true;
        }
        if (this.isInside(mouseX, mouseY, nextX, arrowY, 18, 14)) {
            this.changePage(1);
            return true;
        }

        int chapterX = left + RIGHT_PAGE_X + 8;
        int chapterY = top + PAGE_TOP + 32;
        for (int index = 0; index < CollectibleCatalog.SETS.size(); index++) {
            if (this.isInside(mouseX, mouseY, chapterX - 4, chapterY + index * 21 - 3, PAGE_WIDTH - 8, 18)) {
                this.openPage(index + 1);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderBook(GuiGraphics guiGraphics, int left, int top) {
        int right = left + BOOK_WIDTH;
        int bottom = top + BOOK_HEIGHT;
        int spineX = left + BOOK_WIDTH / 2;

        guiGraphics.fill(left - 5, top - 5, right + 5, bottom + 5, COLOR_SHADOW);
        guiGraphics.fill(left, top, right, bottom, COLOR_COVER);
        guiGraphics.fill(left + 6, top + 6, right - 6, bottom - 6, COLOR_COVER_EDGE);
        guiGraphics.fill(left + 12, top + 12, spineX - 8, bottom - 12, COLOR_PAGE);
        guiGraphics.fill(spineX + 8, top + 12, right - 12, bottom - 12, COLOR_PAGE);

        guiGraphics.fill(spineX - 10, top + 14, spineX - 8, bottom - 14, COLOR_PAGE_SHADE);
        guiGraphics.fill(spineX + 8, top + 14, spineX + 10, bottom - 14, COLOR_PAGE_SHADE);
        guiGraphics.fill(left + 16, top + 32, spineX - 18, top + 35, COLOR_PAGE_LINE);
        guiGraphics.fill(spineX + 20, top + 32, right - 20, top + 35, COLOR_PAGE_LINE);

        for (int y = top + 20; y < bottom - 20; y += 12) {
            guiGraphics.fill(spineX - 2, y, spineX + 2, y + 7, COLOR_COVER);
        }

        this.renderCorner(guiGraphics, left + 14, top + 14, 1, 1);
        this.renderCorner(guiGraphics, spineX - 20, top + 14, -1, 1);
        this.renderCorner(guiGraphics, spineX + 14, top + 14, 1, 1);
        this.renderCorner(guiGraphics, right - 24, top + 14, -1, 1);
        this.renderCorner(guiGraphics, left + 14, bottom - 24, 1, -1);
        this.renderCorner(guiGraphics, right - 24, bottom - 24, -1, -1);
    }

    private void renderCorner(GuiGraphics guiGraphics, int x, int y, int sx, int sy) {
        this.fillDirected(guiGraphics, x, y, x + sx * 10, y + sy, COLOR_PAGE_SHADE);
        this.fillDirected(guiGraphics, x, y, x + sx, y + sy * 10, COLOR_PAGE_SHADE);
        this.fillDirected(guiGraphics, x + sx * 3, y + sy * 3, x + sx * 7, y + sy * 4, COLOR_PAGE_SHADE);
        this.fillDirected(guiGraphics, x + sx * 3, y + sy * 3, x + sx * 4, y + sy * 7, COLOR_PAGE_SHADE);
    }

    private void renderIndex(
            GuiGraphics guiGraphics,
            int left,
            int top,
            int mouseX,
            int mouseY,
            PlayerCollectionProgress progress
    ) {
        int leftX = left + LEFT_PAGE_X;
        int rightX = left + RIGHT_PAGE_X;
        int pageY = top + PAGE_TOP;
        long dayTime = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getDayTime() : 0L;
        CollectibleSetDefinition featuredSet = CollectibleCatalog.featuredSetForDay(dayTime);

        this.drawPageTitle(guiGraphics, leftX, pageY, Component.translatable("collection.journal.book.index"));
        this.drawWrappedLines(guiGraphics, Component.translatable("collection.journal.book.index_body"), leftX + 6, pageY + 28, PAGE_WIDTH - 12, 8, COLOR_TEXT);
        guiGraphics.drawString(this.font, Component.translatable("collection.journal.book.progress"), leftX + 6, pageY + 132, COLOR_TEXT, false);
        this.drawProgressBar(guiGraphics, leftX + 8, pageY + 148, PAGE_WIDTH - 18, 14, progress.discoveredTotal(), CollectibleCatalog.COLLECTIBLES.size());
        this.drawWrappedLines(guiGraphics, Component.translatable("collection.journal.screen.featured", featuredSet.name()), leftX + 6, pageY + 170, PAGE_WIDTH - 12, 2, COLOR_MUTED);

        this.drawPageTitle(guiGraphics, rightX, pageY, Component.translatable("collection.journal.book.chapters"));
        int rowY = pageY + 32;
        for (int index = 0; index < CollectibleCatalog.SETS.size(); index++) {
            CollectibleSetDefinition set = CollectibleCatalog.SETS.get(index);
            int y = rowY + index * 21;
            boolean hovered = this.isInside(mouseX, mouseY, rightX + 4, y - 3, PAGE_WIDTH - 8, 18);
            this.renderSetChapterRow(guiGraphics, rightX, y, set, progress, index + 1, hovered);
        }
    }

    private void renderSet(
            GuiGraphics guiGraphics,
            int left,
            int top,
            int mouseX,
            int mouseY,
            PlayerCollectionProgress progress,
            CollectibleSetDefinition set
    ) {
        int leftX = left + LEFT_PAGE_X;
        int rightX = left + RIGHT_PAGE_X;
        int pageY = top + PAGE_TOP;
        ItemStack rewardStack = set.createRewardStack();
        int discovered = progress.discoveredCount(set);
        boolean complete = progress.isSetComplete(set);
        boolean claimed = progress.hasClaimedReward(set.id());

        this.drawPageTitle(guiGraphics, leftX, pageY, set.name());
        this.drawWrappedLines(
                guiGraphics,
                Component.translatable("collection.journal.book.set_body", set.name(), rewardStack.getHoverName()),
                leftX + 6,
                pageY + 28,
                PAGE_WIDTH - 12,
                7,
                COLOR_TEXT
        );
        guiGraphics.renderFakeItem(rewardStack, leftX + 8, pageY + 104);
        guiGraphics.drawString(this.font, rewardStack.getHoverName(), leftX + 30, pageY + 108, COLOR_TEXT, false);
        guiGraphics.drawString(
                this.font,
                claimed
                        ? Component.translatable("collection.journal.screen.reward_claimed", rewardStack.getHoverName())
                        : Component.translatable("collection.journal.screen.reward_missing"),
                leftX + 6,
                pageY + 130,
                claimed ? COLOR_GOOD : COLOR_MUTED,
                false
        );
        guiGraphics.drawString(this.font, Component.translatable("collection.journal.book.progress"), leftX + 6, pageY + 148, COLOR_TEXT, false);
        this.drawProgressBar(guiGraphics, leftX + 8, pageY + 164, PAGE_WIDTH - 18, 14, discovered, set.size());

        this.drawPageTitle(guiGraphics, rightX, pageY, Component.translatable("collection.journal.book.entries"));
        int rowY = pageY + 32;
        List<CollectibleDefinition> collectibles = set.collectibles();
        for (int index = 0; index < collectibles.size(); index++) {
            CollectibleDefinition collectible = collectibles.get(index);
            int y = rowY + index * 36;
            boolean hovered = this.isInside(mouseX, mouseY, rightX + 4, y - 4, PAGE_WIDTH - 8, 30);
            this.renderCollectibleRow(guiGraphics, rightX, y, mouseX, mouseY, collectible, progress, hovered);
        }

        guiGraphics.drawString(
                this.font,
                complete
                        ? Component.translatable("collection.journal.screen.complete")
                        : Component.translatable("collection.journal.screen.incomplete"),
                rightX + 8,
                pageY + 160,
                complete ? COLOR_GOOD : COLOR_WARN,
                false
        );

        if (mouseX >= leftX + 8 && mouseX <= leftX + 24 && mouseY >= pageY + 104 && mouseY <= pageY + 120) {
            guiGraphics.renderTooltip(this.font, rewardStack, mouseX, mouseY);
        }
    }

    private void renderSetChapterRow(
            GuiGraphics guiGraphics,
            int x,
            int y,
            CollectibleSetDefinition set,
            PlayerCollectionProgress progress,
            int targetPage,
            boolean hovered
    ) {
        if (hovered || this.pageIndex == targetPage) {
            guiGraphics.fill(x + 2, y - 4, x + PAGE_WIDTH - 4, y + 16, COLOR_HOVER);
        }

        ItemStack rewardStack = set.createRewardStack();
        int discovered = progress.discoveredCount(set);
        boolean complete = progress.isSetComplete(set);
        guiGraphics.renderFakeItem(rewardStack, x + 8, y - 3);
        guiGraphics.drawString(this.font, set.name(), x + 30, y, COLOR_TEXT, false);
        guiGraphics.drawString(this.font, Component.literal(discovered + "/" + set.size()), x + PAGE_WIDTH - 28, y, complete ? COLOR_GOOD : COLOR_MUTED, false);
    }

    private void renderCollectibleRow(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int mouseX,
            int mouseY,
            CollectibleDefinition collectible,
            PlayerCollectionProgress progress,
            boolean hovered
    ) {
        if (hovered) {
            guiGraphics.fill(x + 2, y - 4, x + PAGE_WIDTH - 4, y + 30, COLOR_HOVER);
        }

        ItemStack stack = collectible.item().get().getDefaultInstance();
        boolean found = progress.hasDiscovered(collectible.id());
        guiGraphics.renderFakeItem(stack, x + 8, y - 2);
        guiGraphics.drawString(this.font, stack.getHoverName(), x + 30, y, COLOR_TEXT, false);
        guiGraphics.drawString(
                this.font,
                found
                        ? Component.translatable("collection.journal.item_found", stack.getHoverName())
                        : Component.translatable("collection.journal.item_missing", stack.getHoverName()),
                x + 30,
                y + 12,
                found ? COLOR_GOOD : COLOR_MUTED,
                false
        );

        if (hovered) {
            guiGraphics.renderTooltip(this.font, List.of(stack.getHoverName(), Component.translatable("collection.journal.clue", collectible.clue())), stack.getTooltipImage(), stack, mouseX, mouseY);
        }
    }

    private void renderPageArrows(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int previousX = left + 172;
        int nextX = left + BOOK_WIDTH - 28;
        int y = top + BOOK_HEIGHT - 20;
        int previousColor = this.pageIndex > 0 ? COLOR_PAGE : COLOR_PAGE_SHADE;
        int nextColor = this.pageIndex < this.pageCount() - 1 ? COLOR_PAGE : COLOR_PAGE_SHADE;

        this.drawArrow(guiGraphics, previousX, y, false, previousColor);
        this.drawArrow(guiGraphics, nextX, y, true, nextColor);
    }

    private void drawArrow(GuiGraphics guiGraphics, int x, int y, boolean right, int color) {
        if (right) {
            guiGraphics.fill(x, y + 5, x + 12, y + 9, color);
            guiGraphics.fill(x + 8, y + 2, x + 16, y + 12, color);
            guiGraphics.fill(x + 12, y, x + 18, y + 14, color);
            guiGraphics.fill(x, y + 9, x + 12, y + 11, COLOR_PROGRESS_FRAME);
            return;
        }

        guiGraphics.fill(x - 12, y + 5, x, y + 9, color);
        guiGraphics.fill(x - 16, y + 2, x - 8, y + 12, color);
        guiGraphics.fill(x - 18, y, x - 12, y + 14, color);
        guiGraphics.fill(x - 12, y + 9, x, y + 11, COLOR_PROGRESS_FRAME);
    }

    private void fillDirected(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2), color);
    }

    private void drawPageTitle(GuiGraphics guiGraphics, int x, int y, Component title) {
        guiGraphics.drawCenteredString(this.font, title, x + PAGE_WIDTH / 2, y, COLOR_TEXT);
        guiGraphics.fill(x + 8, y + 16, x + PAGE_WIDTH - 8, y + 18, COLOR_PAGE_LINE);
    }

    private void drawProgressBar(GuiGraphics guiGraphics, int x, int y, int width, int height, int value, int max) {
        int innerWidth = width - 4;
        int fillWidth = max <= 0 ? 0 : Mth.clamp(value * innerWidth / max, 0, innerWidth);

        guiGraphics.fill(x, y, x + width, y + height, COLOR_PROGRESS_FRAME);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, COLOR_PROGRESS_BACK);
        guiGraphics.fill(x + 2, y + 2, x + 2 + fillWidth, y + height - 2, COLOR_PROGRESS_FILL);
    }

    private void drawWrappedLines(GuiGraphics guiGraphics, Component component, int x, int y, int width, int maxLines, int color) {
        List<FormattedCharSequence> lines = this.font.split(component, width);
        int lineCount = Math.min(lines.size(), maxLines);
        for (int index = 0; index < lineCount; index++) {
            guiGraphics.drawString(this.font, lines.get(index), x, y + index * LINE_HEIGHT, color, false);
        }
    }

    private void changePage(int delta) {
        this.openPage(this.pageIndex + delta);
    }

    private void openPage(int targetPage) {
        this.pageIndex = Mth.clamp(targetPage, 0, this.pageCount() - 1);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private int pageCount() {
        return 1 + CollectibleCatalog.SETS.size();
    }

    private int pageLeft() {
        return (this.width - BOOK_WIDTH) / 2;
    }

    private int pageTop() {
        return (this.height - BOOK_HEIGHT) / 2;
    }
}
