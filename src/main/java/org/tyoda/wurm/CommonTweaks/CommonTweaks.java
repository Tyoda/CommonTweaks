package org.tyoda.wurm.CommonTweaks;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.items.*;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import javassist.ClassPool;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.tyoda.wurm.CommonTweaks.actions.ReloadConfigAction;
import org.tyoda.wurmunlimited.mods.CommonLibrary.SimpleProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class CommonTweaks implements WurmServerMod, Configurable, PreInitable, Initable, Versioned, ServerStartedListener {
    public static final Logger logger = Logger.getLogger(CommonTweaks.class.getName());
    public static final String version = "0.2";
    public static final Path configPath = Paths.get("mods/CommonTweaks.config");

    // Sound Timer module
    private boolean soundTimerModule = false;
    private int soundMillis = 5000;
    private final HashMap<Long, HashMap<String, Long>> soundData = new HashMap<>();

    // TODO: remove player from soundData when they log out

    // Adjust render distance module
    private boolean adjustRenderDistanceModule = true;

    private boolean alwaysAdjustUnfinished = true;

    private final HashMap<Integer, Integer> adjustRenderMap = new HashMap<>();

    // other stuff
    private boolean loadedConfig = false;
    private int reloadGmPower = 4;
    private static CommonTweaks instance;

    public CommonTweaks(){
        instance = this;
    }

    @Override
    public void configure(Properties properties) {
        logger.info("Starting configure");
        SimpleProperties p = new SimpleProperties(properties);
        doConfig(p);
        loadedConfig = true;
        logger.info("Done with configure");
    }

    public void doConfig(SimpleProperties p){
        reloadGmPower = p.getInt("reloadGMPower", 4);

        // Sound Timer module
        if(!loadedConfig)
            soundTimerModule = p.getBoolean("enableSoundTimer", soundTimerModule);
        if(soundTimerModule) {
            int pSoundMillis = p.getInt("sTMillis", soundMillis);
            if (pSoundMillis > 0) soundMillis = pSoundMillis;
        }

        // Adjust render distance module
        if(!loadedConfig)
            adjustRenderDistanceModule = p.getBoolean("enableAdjustRenderDistance", adjustRenderDistanceModule);
        if(adjustRenderDistanceModule){
            alwaysAdjustUnfinished = p.getBoolean("aRDAlwaysAdjustUnfinished", alwaysAdjustUnfinished);

            adjustRenderMap.clear();
            String[] adjustRenders = p.getStringArray("aRDItems", new String[0], ",");
            for(String adjustRender : adjustRenders){
                if(adjustRender.equals("")) continue;

                String[] keyValue = adjustRender.split(";");
                adjustRenderMap.put(Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]));
                logger.info("Adjusted item id "+keyValue[0]+" render distance to "+keyValue[1]);
            }
        }
    }

    @Override
    public void preInit() {
        ModActions.init();
    }

    @Override
    public void init() {
        logger.info("Starting init");

        HookManager hM = HookManager.getInstance();
        ClassPool classPool = hM.getClassPool();

        // Sound Timer module
        if(soundTimerModule) {
            logger.info("Injecting Sound Timer module");
            hM.registerHook(
                "com.wurmonline.server.behaviours.Action",
                "mayPlaySound",
                "()Z",
                () ->
                    (proxy, method, args) -> {
                        Action action = (Action)proxy;
                        if(action.getPerformer().isPlayer()){
                            String actionString = action.getActionString();
                            long currentMillis = System.currentTimeMillis();
                            // Create entry for player if absent
                            HashMap<String, Long> playerData = soundData.computeIfAbsent(action.getPerformer().getWurmId(), k -> new HashMap<>());
                            // Create entry for action sound for player if absent
                            playerData.putIfAbsent(actionString, 0L);

                            // see if 5 seconds have passed since last sound was played
                            boolean playSound = currentMillis >= playerData.get(actionString) + soundMillis;
                            if(playSound)
                                playerData.put(actionString, currentMillis);

                            return playSound;
                        }else{
                            return action.currentSecond() % 5 == 0;
                        }
            });
            logger.info("Successfully injected Sound Timer module");
        }else logger.info("Skipping Sound Timer module.");

        // Unfinished render distance

        if(adjustRenderDistanceModule){
            logger.info("Injecting Adjust Render Distance module");
            hM.registerHook(
                    "com.wurmonline.server.zones.VirtualZone",
                    "isVisible",
                    "(Lcom/wurmonline/server/items/Item;Lcom/wurmonline/server/zones/VolaTile;)Z",
                    () ->
                        (proxy, method, args) -> {
                            final int templateId = ((Item)args[0]).getTemplateId();

                            Integer adjusted = adjustRenderMap.get(templateId);
                            if(templateId == 179){
                                final int finishedTemplateId = AdvancedCreationEntry.getTemplateId((Item) args[0]);
                                adjusted = adjustRenderMap.get(finishedTemplateId);
                                if(alwaysAdjustUnfinished && adjusted == null){
                                    int iSize;
                                    try {
                                        iSize = ItemTemplateFactory.getInstance().getTemplate(AdvancedCreationEntry.getTemplateId((Item) args[0])).getSizeZ();
                                    } catch(NoSuchTemplateException e){
                                        return method.invoke(proxy, args);
                                    }
                                    adjusted = 3;
                                    if(iSize >= 500){
                                        return true;
                                    } else if (iSize >= 300) {
                                        adjusted = 128;
                                    } else if (iSize >= 200) {
                                        adjusted = 64;
                                    } else if (iSize >= 100) {
                                        adjusted = 32;
                                    } else if (iSize >= 50) {
                                        adjusted = 16;
                                    } else if (iSize >= 10) {
                                        adjusted = 8;
                                    }
                                }
                            }

                            if(adjusted != null){
                                VolaTile tile = (VolaTile) args[1];
                                int distanceX = Math.abs(tile.getTileX() - ((VirtualZone)proxy).getCenterX());
                                int distanceY = Math.abs(tile.getTileY() - ((VirtualZone)proxy).getCenterY());
                                return Math.max(distanceX, distanceY) <= adjusted;
                            }else{
                                return method.invoke(proxy, args);
                            }
                        }
            );
            logger.info("Successfully injected Adjust Render Distance module");
        }

        logger.info("Done with init");
    }

    @Override
    public void onServerStarted(){
        ModActions.registerAction(new ReloadConfigAction());
    }

    public String getVersion(){
        return version;
    }

    public static CommonTweaks getInstance(){
        return instance;
    }

    public int getReloadGmPower() {
        return reloadGmPower;
    }

    // TODO: add life preserver
    // TODO: add digtocorner action
    // TODO: rare pottery planters give better ql?
    // TODO: lamp/candelabra hanging from ceiling
    // TODO: window flower boxes //more than one flower? //planter pots set in them???
    // TODO: hanging flower baskets like hanging lamps //more than one flower? //planter pots set in them???
    // TODO: Potter's bench
    // TODO: strongwall reinforce tile instead of collapsing it
}
