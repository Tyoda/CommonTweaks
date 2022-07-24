package org.tyoda.wurmunlimited.mods.CommonTweaks.actions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.*;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.tyoda.wurmunlimited.mods.CommonTweaks.CommonTweaks;
import org.tyoda.wurmunlimited.mods.CommonTweaks.LootTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BreakPiggyBankAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final ActionEntry actionEntry;
    private final short actionId;
    public static final ArrayList<Integer> breakOn = new ArrayList<>();
    public BreakPiggyBankAction(){
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(this.actionId, "Break the bank",
                "breaking the bank", new int[]{Actions.ACTION_TYPE_MAYBE_USE_ACTIVE_ITEM});
        ModActions.registerAction(actionEntry);
    }
    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, int tile, int dir) {
        CommonTweaks.logger.info("morp " + performer.isOnSurface() + ", tile: "+Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)));
        if(object.getTemplateId() == CommonTweaks.getPiggyBankTemplateId() && (
               !performer.isOnSurface()
            || Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) == Tiles.TILE_TYPE_ROCK))
            return Collections.singletonList(actionEntry);
        return null;
    }
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {
        if(subject.getTemplateId() != CommonTweaks.getPiggyBankTemplateId() || !breakOn.contains(target.getTemplateId())
            || !target.isOnSurface())
            return null;
        return Collections.singletonList(actionEntry);
    }
    public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, int dir, short num, float counter) {
        CommonTweaks.logger.info("beep");
        return this.action(action, performer, source, tilex, tiley, onSurface, heightOffset, tile, num, counter);
    }
    public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short num, float counter) {
        CommonTweaks.logger.info("boop " + performer.isOnSurface() + ", tile: "+Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)));
        if(performer.isOnSurface()
                || Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) != Tiles.TILE_TYPE_ROCK)
            return defaultPropagation(action);
        return breakPiggy(performer, source, tilex, tiley);
    }
    public boolean action(Action action, Creature performer, Item subject, Item target, short num, float counter) {
        CommonTweaks.logger.info("sheep " + target.isOnSurface());
        if(subject.getTemplateId() != CommonTweaks.getPiggyBankTemplateId() || !breakOn.contains(target.getTemplateId())
                || !target.isOnSurface())
            return defaultPropagation(action);
        return breakPiggy(performer, subject, target.getTileX(), target.getTileY());
    }
    private boolean breakPiggy(Creature performer, Item piggyBank, int tilex, int tiley){
        CommonTweaks.logger.info("Herro!");
        ArrayList<Integer> coins = LootTable.generateCoins(piggyBank.getData1());
        for(int coinId : coins){
            try {
                Item coin = ItemFactory.createItem(coinId, getQuality(), (byte) 0, null);
                performer.getInventory().insertItem(coin);
            }catch(NoSuchTemplateException | FailedException e){CommonTweaks.logger.severe(e.getMessage());}
        }
        return true;
    }
    private float getQuality(){
        return LootTable.random.nextInt(100)+LootTable.random.nextFloat();
    }
    public ActionEntry getActionEntry(){
        return  this.actionEntry;
    }
    @Override
    public short getActionId() {
        return this.actionId;
    }
}
