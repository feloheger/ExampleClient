package com.example.client.module.modules.combat;

import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoTotem: Packt automatisch ein Totem der Unsterblichkeit in die Offhand,
 * sobald diese leer ist oder ein anderes Item hält.
 */
public class AutoTotem extends Module {

    public AutoTotem() {
        super("AutoTotem", "Legt automatisch Totems in die Offhand", Category.COMBAT);
        // FIX: Nur einmal bei der Initialisierung des Clients registrieren, um Abstürze zu vermeiden
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    private void onTick(MinecraftClient mc) {
        // Sicherstellen, dass das Modul aktiv ist und der Spieler existiert
        if (!isEnabled()) return;
        if (mc.player == null || mc.interactionManager == null) return;

        // Wenn die Offhand bereits ein Totem hält, müssen wir nichts tun
        ItemStack offhand = mc.player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.isOf(Items.TOTEM_OF_UNDYING)) return;

        PlayerInventory inv = mc.player.getInventory();

        // Durchsuche das gesamte Inventar nach einem Totem
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.TOTEM_OF_UNDYING)) {

                // Minecraft-Slot-Index in Screen-Container-Slot-Index umrechnen
                int slotId = i;
                if (i < 9) {
                    // Hotbar-Slots (0-8) liegen im Container-System auf den Slots 36-44
                    slotId = i + 36;
                } else if (i < 36) {
                    // Normales Inventar bleibt gleich, muss aber nicht verschoben werden
                    slotId = i;
                } else if (i == 40) {
                    // Slot 40 ist bereits die Offhand
                    continue;
                }

                // FIX: Nutze den interactionManager, um das Item sauber via Paket-Klick in die Offhand (Slot 45) zu legen
                // Parameter: syncId, slotId, button (40 steht für Offhand-Swap-Keybind oder speziellen Klick), ActionType, Player
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        slotId,
                        40,
                        SlotActionType.SWAP,
                        mc.player
                );

                break; // Aktion ausgeführt, Schleife abbrechen
            }
        }
    }
}