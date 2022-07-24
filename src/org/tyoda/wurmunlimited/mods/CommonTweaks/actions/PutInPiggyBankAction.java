package org.tyoda.wurmunlimited.mods.CommonTweaks.actions;

import com.wurmonline.server.Items;
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

import java.util.Collections;
import java.util.List;

public class PutInPiggyBankAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final ActionEntry actionEntry;
    private final short actionId;
    public PutInPiggyBankAction(){
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(this.actionId, "Deposit",
                "depositing", new int[]{Actions.ACTION_TYPE_MAYBE_USE_ACTIVE_ITEM});
        ModActions.registerAction(actionEntry);
    }
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {
        Item coin = subject;
        Item piggy = target;
        if(coin != null && !coin.isCoin()){
            coin = target;
            piggy = subject;
        }
        if(!performer.isPlayer() || coin == null || !coin.isCoin() || piggy == null
                || piggy.getTemplateId() != CommonTweaks.getPiggyBankTemplateId()) return null;
        return Collections.singletonList(actionEntry);
    }
    public boolean action(Action action, Creature performer, Item subject, Item target, short num, float counter) {
        Item coin = subject;
        Item piggy = target;
        if(coin != null && !coin.isCoin()){
            coin = target;
            piggy = subject;
        }
        if (!performer.isPlayer() || coin == null || !coin.isCoin() ||
                piggy.getTemplateId() != CommonTweaks.getPiggyBankTemplateId())
            return defaultPropagation(action);
        return depositCoin(performer, coin, piggy);
    }
    public static boolean depositCoin(Creature performer, Item coin, Item piggyBank){
        int coinValue = coin.getValue();
        if(piggyBank.getData1() == -1) piggyBank.setData1(0);
        piggyBank.setData1(piggyBank.getData1() + coinValue);
        piggyBank.setWeight(piggyBank.getWeightGrams(false)+coin.getWeightGrams(false), false, true);
        Items.destroyItem(coin.getWurmId(), false, true);
        performer.getCommunicator().
                sendSafeServerMessage("You deposit the " + Item.getMaterialString(coin.getMaterial()) + " " + coin.getName() + " in the piggy bank.");
        return true;
    }

    public ActionEntry getActionEntry(){
        return  this.actionEntry;
    }
    @Override
    public short getActionId() {
        return this.actionId;
    }
}
