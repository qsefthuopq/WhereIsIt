package red.jackf.whereisit.api;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Entrypoint for a plugin.
 * @see red.jackf.whereisit.VanillaItemBehaviors VanillaItemBehaviors.
 */
public interface WhereIsItEntrypoint {

    /**
     * Set up custom item behaviors for items in chest.
     * Intended for items with inventories such as shulker boxes, backpacks, etc.
     *
     * @param behaviors The list of behaviors to append to. The behavior will only be ran if its predicate succeeds.
     */

    @ApiStatus.OverrideOnly
    default void setupItemBehaviors(Map<Predicate<ItemStack>, CustomItemBehavior> behaviors) {

    }

    /**
     * Set up custom block behaviors for blocks in world.
     * Intended for blocks that hold items in world that do not implement {@link net.minecraft.inventory.Inventory}
     *
     * @param behaviors The map of behaviors to append to. The BiPredicate is where you would check that the block
     *                  could potentially hold an item, and is called with the BlockState and BlockEntity (which could
     *                  be null).
     */

    @ApiStatus.OverrideOnly
    default void setupWorldBehaviors(Map<BiPredicate<BlockState, BlockEntity>, CustomWorldBehavior> behaviors) {
    }

    /**
     * @return The priority of this plugin to load. Plugins will load in ascending order (0 first, 1 second, ...)
     */
    @ApiStatus.OverrideOnly
    default int getPriority() {
        return 0;
    }
}
