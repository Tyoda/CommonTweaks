package org.tyoda.wurm.CommonTweaks.actions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.tyoda.wurm.CommonTweaks.CommonTweaks;
import org.tyoda.wurmunlimited.mods.CommonLibrary.SimpleProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReloadConfigAction implements ModAction, ActionPerformer, BehaviourProvider {
    public static final Logger logger = CommonTweaks.logger;
    public final short actionId;
    public final ActionEntry actionEntry;

    public ReloadConfigAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Reload CommonTweaks", "Reloading CommonTweaks", new int[]{Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }

    private List<ActionEntry> getBehaviour(Creature performer, Item activated){
        if(performer.getPower() < CommonTweaks.getInstance().getReloadGmPower() || (activated.getTemplateId() != 315 && activated.getTemplateId() != 176))
            return null; // not gm or not gm wand

        return Collections.singletonList(actionEntry);
    }

    private boolean doAction(Creature performer, Item activated){
        if(performer.getPower() < CommonTweaks.getInstance().getReloadGmPower() || (activated.getTemplateId() != 315 && activated.getTemplateId() != 176))
            return true; // not gm or not gm wand

        // courtesy of Daniel Monzert's implementation from the Treasure Hunting mod

        if (!Files.exists(CommonTweaks.configPath)) {
            performer.getCommunicator().sendAlertServerMessage("The config file seems to be missing.");
            return true;
        }

        InputStream stream = null;

        try {
            // performer.getCommunicator().sendAlertServerMessage("Opening the config file.");
            stream = Files.newInputStream(CommonTweaks.configPath);
            Properties properties = new Properties();

            // performer.getCommunicator().sendAlertServerMessage("Reading from the config file.");
            properties.load(stream);
            SimpleProperties p = new SimpleProperties(properties);

            logger.info("Reloading configuration.");
            // performer.getCommunicator().sendAlertServerMessage("Loading all options.");
            CommonTweaks.getInstance().doConfig(p);

            logger.info("Configuration reloaded.");
            performer.getCommunicator().sendAlertServerMessage("The config file has been reloaded.");
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Error while reloading config file.", ex);
            performer.getCommunicator().sendAlertServerMessage("Error reloading the config file, check the server log.");
        }
        finally {
            try {
                if (stream != null)
                    stream.close();
            }
            catch (IOException ex) {
                logger.log(Level.SEVERE, "Config file not closed, possible file lock.", ex);
                performer.getCommunicator().sendAlertServerMessage("Error closing the config file, possible file lock.");
            }
        }

        return true;
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, Tiles.TileBorderDirection dir, long borderId, short num, float counter) {
        return doAction(performer, source);
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset, short num, float counter) {
        return doAction(performer, source);
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, int id, short num, float counter) {
        return doAction(performer, source);
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return doAction(performer, source);
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short num, float counter) {
        return doAction(performer, source);
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, int dir, short num, float counter) {
        return doAction(performer, source);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {
        return getBehaviour(performer, subject);
    }
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset) {
        return getBehaviour(performer, object);
    }
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset) {
        return getBehaviour(performer, object);
    }
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, int tile, int dir) {
        return getBehaviour(performer, object);
    }
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, int tile) {
        return getBehaviour(performer, object);
    }

    @Override
    public short getActionId() {
        return actionId;
    }
}
