package org.tyoda.wurm.CommonTweaks;

import com.wurmonline.server.behaviours.Action;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.Versioned;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.tyoda.wurmunlimited.mods.CommonLibrary.SimpleProperties;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class CommonTweaks implements WurmServerMod, Configurable, Initable, Versioned {
    public static final Logger logger = Logger.getLogger(CommonTweaks.class.getName());
    public static final String version = "0.1";
    // Sound Timer module
    private boolean soundTimerModule = false;
    private int soundMillis = 5000;
    private final HashMap<Long, HashMap<String, Long>> soundData = new HashMap<>();

    @Override
    public void configure(Properties properties) {
        logger.info("Starting configure");
        SimpleProperties p = new SimpleProperties(properties);

        // Sound Timer module
        soundTimerModule = p.getBoolean("enableSoundTimer", soundTimerModule);
        if(soundTimerModule) {
            int pSoundMillis = p.getInt("sTMillis", soundMillis);
            if (pSoundMillis > 0) soundMillis = pSoundMillis;
        }

        logger.info("Done with configure");
    }

    @Override
    public void init() {
        logger.info("Starting init");
        HookManager hM = HookManager.getInstance();
        //ClassPool classPool = hM.getClassPool();

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

        logger.info("Done with init");
    }

    public String getVersion(){
        return version;
    }

    // TODO: add life preserver
    // TODO: add digtocorner action
    // TODO: rare pottery planters give better ql?
    // TODO: lamp/candelabra hanging from ceiling
    // TODO: window flower boxes //more than one flower? //planter pots set in them???
    // TODO: hanging flower baskets like hanging lamps //more than one flower? //planter pots set in them???
    // TODO: Potter's bench
}
