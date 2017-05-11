/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.destinationsol.game.screens;

import org.destinationsol.GameOptions;
import org.destinationsol.SolApplication;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.item.ItemContainer;
import org.destinationsol.game.item.SolItem;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.ui.SolInputManager;
import org.destinationsol.ui.SolUiControl;
import org.destinationsol.ui.UiDrawer;

import java.util.ArrayList;
import java.util.List;

public class SellItems implements InventoryOperations {

    public static float PERC = .8f;
    public final SolUiControl sellCtrl;
    private final ArrayList<SolUiControl> myControls;

    public SellItems(InventoryScreen inventoryScreen, GameOptions gameOptions) {
        myControls = new ArrayList<SolUiControl>();

        sellCtrl = new SolUiControl(inventoryScreen.itemCtrl(0), true, gameOptions.getKeySellItem());
        sellCtrl.setDisplayName("Sell");
        myControls.add(sellCtrl);
    }

    @Override
    public ItemContainer getItems(SolGame game) {
        SolShip h = game.getHero();
        return h == null ? null : h.getItemContainer();
    }

    @Override
    public boolean isUsing(SolGame game, SolItem item) {
        SolShip h = game.getHero();
        return h != null && h.maybeUnequip(game, item, false);
    }

    @Override
    public float getPriceMul() {
        return PERC;
    }

    @Override
    public String getHeader() {
        return "Sell:";
    }

    @Override
    public List<SolUiControl> getControls() {
        return myControls;
    }

    @Override
    public void updateCustom(SolApplication cmp, SolInputManager.Ptr[] ptrs, boolean clickedOutside) {
        SolGame game = cmp.getGame();
        InventoryScreen is = game.getScreens().inventoryScreen;
        TalkScreen talkScreen = game.getScreens().talkScreen;
        SolShip target = talkScreen.getTarget();
        SolShip hero = game.getHero();
        if (talkScreen.isTargetFar(hero)) {
            cmp.getInputMan().setScreen(cmp, game.getScreens().mainScreen);
            return;
        }
        SolItem selItem = is.getSelectedItem();
        if (selItem == null) {
            sellCtrl.setDisplayName("----");
            sellCtrl.setEnabled(false);
            return;
        }

        boolean isWornAndCanBeSold = isItemEquippedAndSellable(selItem, target, game, cmp.getOptions());
        boolean enabled = isItemSellable(selItem, target);

        if (enabled && isWornAndCanBeSold) {
            sellCtrl.setDisplayName("Sell");
            sellCtrl.setEnabled(true);
        } else if (enabled && !isWornAndCanBeSold) {
            sellCtrl.setDisplayName("Unequip it!");
            sellCtrl.setEnabled(false);
        } else {
            sellCtrl.setDisplayName("----");
            sellCtrl.setEnabled(false);
        }

        if (!enabled || !isWornAndCanBeSold) {
            return;
        }
        if (sellCtrl.isJustOff()) {
            ItemContainer ic = hero.getItemContainer();
            is.setSelected(ic.getSelectionAfterRemove(is.getSelected()));
            ic.remove(selItem);
            target.getTradeContainer().getItems().add(selItem);
            hero.setMoney(hero.getMoney() + selItem.getPrice() * PERC);
        }
    }

    private boolean isItemSellable(SolItem item, SolShip target) {
        return target.getTradeContainer().getItems().canAdd(item);
    }

    // return true if the item is not worn, or is worn and canSellEquippedItems is true
    private boolean isItemEquippedAndSellable(SolItem item, SolShip target, SolGame game, GameOptions options) {
        if (item.isEquipped() == 0 || ((item.isEquipped() != 0) && options.canSellEquippedItems)) {
            return true;
        }
        return false;
    }

    @Override
    public void drawBg(UiDrawer uiDrawer, SolApplication cmp) {

    }

    @Override
    public void drawImgs(UiDrawer uiDrawer, SolApplication cmp) {

    }

    @Override
    public void drawText(UiDrawer uiDrawer, SolApplication cmp) {

    }

    @Override
    public boolean reactsToClickOutside() {
        return false;
    }

    @Override
    public boolean isCursorOnBg(SolInputManager.Ptr ptr) {
        return false;
    }

    @Override
    public void onAdd(SolApplication cmp) {

    }

    @Override
    public void blurCustom(SolApplication cmp) {

    }
}
