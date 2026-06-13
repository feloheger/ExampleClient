package com.example.client.module.settings;

import com.example.client.module.modules.gui.BlockSelectorScreen;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class BlockListSetting extends Setting<List<Block>> {

    public BlockListSetting(String name, String description, List<Block> defaultValue) {
        super(name, description, new ArrayList<>(defaultValue));
    }

    public boolean contains(Block block) {
        return getValue().contains(block);
    }

    public void add(Block block) {
        if (!getValue().contains(block)) getValue().add(block);
    }

    public void remove(Block block) {
        getValue().remove(block);
    }

    /** Öffnet den Block-Selector-Screen */
    public void openScreen() {
        MinecraftClient.getInstance().setScreen(
                new BlockSelectorScreen(this, null)
        );
    }

    @Override
    public List<Block> getValue() { return super.getValue(); }
}