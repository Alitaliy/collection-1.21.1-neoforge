package com.collection.progress;

import com.collection.collectible.CollectibleSetDefinition;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class PlayerCollectionProgress implements INBTSerializable<CompoundTag> {
    private static final String DISCOVERED_KEY = "Discovered";
    private static final String REWARDED_KEY = "Rewarded";
    private static final String MILESTONES_KEY = "Milestones";

    private final Set<String> discovered = new HashSet<>();
    private final Set<String> rewardedSets = new HashSet<>();
    private final Set<String> claimedMilestones = new HashSet<>();

    public boolean discover(ResourceLocation collectibleId) {
        return this.discovered.add(collectibleId.toString());
    }

    public boolean hasDiscovered(ResourceLocation collectibleId) {
        return this.discovered.contains(collectibleId.toString());
    }

    public int discoveredCount(CollectibleSetDefinition set) {
        return (int) set.collectibles().stream().filter(collectible -> this.hasDiscovered(collectible.id())).count();
    }

    public boolean isSetComplete(CollectibleSetDefinition set) {
        return this.discoveredCount(set) == set.size();
    }

    public boolean claimReward(String setId) {
        return this.rewardedSets.add(setId);
    }

    public boolean hasClaimedReward(String setId) {
        return this.rewardedSets.contains(setId);
    }

    public boolean claimMilestone(String setId, int stage) {
        return this.claimedMilestones.add(setId + "#" + stage);
    }

    public boolean hasClaimedMilestone(String setId, int stage) {
        return this.claimedMilestones.contains(setId + "#" + stage);
    }

    public int discoveredTotal() {
        return this.discovered.size();
    }

    public Set<String> discoveredEntries() {
        return Set.copyOf(this.discovered);
    }

    public Set<String> rewardedEntries() {
        return Set.copyOf(this.rewardedSets);
    }

    public Set<String> milestoneEntries() {
        return Set.copyOf(this.claimedMilestones);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put(DISCOVERED_KEY, this.writeStringList(this.discovered));
        tag.put(REWARDED_KEY, this.writeStringList(this.rewardedSets));
        tag.put(MILESTONES_KEY, this.writeStringList(this.claimedMilestones));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.discovered.clear();
        this.rewardedSets.clear();
        this.claimedMilestones.clear();
        this.readStringList(nbt.getList(DISCOVERED_KEY, Tag.TAG_STRING), this.discovered);
        this.readStringList(nbt.getList(REWARDED_KEY, Tag.TAG_STRING), this.rewardedSets);
        this.readStringList(nbt.getList(MILESTONES_KEY, Tag.TAG_STRING), this.claimedMilestones);
    }

    private ListTag writeStringList(Set<String> values) {
        ListTag listTag = new ListTag();
        values.stream().sorted().map(StringTag::valueOf).forEach(listTag::add);
        return listTag;
    }

    private void readStringList(ListTag listTag, Set<String> output) {
        for (Tag tag : listTag) {
            output.add(tag.getAsString());
        }
    }
}
