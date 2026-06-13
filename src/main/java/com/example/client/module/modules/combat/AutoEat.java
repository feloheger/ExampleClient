package com.example.client.module.modules.combat;

import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket; // NEU: Paket-Import
import net.minecraft.util.Hand;

/**
 * AutoEat: Isst automatisch, wenn der Hunger unter einen Schwellenwert fällt.
 */
public class AutoEat extends Module {

    private static final int HUNGER_THRESHOLD = 16;

    public AutoEat() {
        super("AutoEat", "Isst automatisch bei niedrigem Hunger", Category.COMBAT);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    private void onTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        if (mc.player == null) return;

        int hunger = mc.player.getHungerManager().getFoodLevel();
        if (hunger >= HUNGER_THRESHOLD) return;
        if (mc.player.isUsingItem()) return;

        PlayerInventory inv = mc.player.getInventory();

        int bestSlot = -1;
        int bestHungerValue = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            FoodComponent food = stack.get(DataComponentTypes.FOOD);
            if (food == null) continue;

            // Falls 'nutrition()' bei dir einen Fehler wirft, ersetze es durch 'getNutrition()'
            int nutritionValue = food.nutrition();

            if (nutritionValue > bestHungerValue) {
                bestHungerValue = nutritionValue;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) return;

        // FIX FÜR PRIVATE ACCESS:
        // Wir senden ein Paket an den Server, um den Slot zu wechseln.
        // Das zwingt den Client und Server dazu, den ausgewählten Slot synchron auf 'bestSlot' zu setzen,
        // ohne dass wir das private Feld 'selectedSlot' direkt anfassen müssen!
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(bestSlot));

            // Drücken der rechten Maustaste simulieren
            mc.options.useKey.setPressed(true);
        }

        if (mc.interactionManager != null) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}