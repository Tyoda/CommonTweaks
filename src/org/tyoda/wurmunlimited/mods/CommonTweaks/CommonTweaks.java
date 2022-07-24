package org.tyoda.wurmunlimited.mods.CommonTweaks;

import com.wurmonline.server.items.*;
import javassist.*;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.modsupport.items.ModItems;
import org.tyoda.wurmunlimited.mods.CommonTweaks.actions.BreakPiggyBankAction;
import org.tyoda.wurmunlimited.mods.CommonTweaks.actions.PutInPiggyBankAction;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

public class CommonTweaks implements WurmServerMod, ServerStartedListener, Configurable, ItemTemplatesCreatedListener, PreInitable {
    public static final Logger logger = Logger.getLogger(CommonTweaks.class.getName());
    public static final Random random = new Random();
    public static final String version = "0.1";
    private static int piggyBankTemplateId = -1;
    private static CommonTweaks instance = null;
    @Override
    public void configure(Properties p) {
        if(instance == null) instance = this;
        // TODO: configure what piggy breaks on
        BreakPiggyBankAction.breakOn.add(185);
    }
    @Override
    public void onItemTemplatesCreated() {
        logger.info("Starting onItemTemplatesCreated");
        try {
            ModItems.init();
            ItemTemplate piggyBankClayTemplate = new ItemTemplateBuilder("ClayPiggyBank").name("clay piggy bank", "clay piggy banks", "A piggy bank that could be fired in a kiln.").itemTypes(new short[]{108, 196, 44, 147, 194, 63, 1}).imageNumber((short)0).decayTime(Long.MAX_VALUE).containerSize(20, 20, 20).modelName("model.creature.quadraped.pig.").difficulty(5.0f).material((byte)18).weightGrams(750).behaviourType((short)1).build();
            ItemTemplate piggyBankPotteryTemplate = new ItemTemplateBuilder("PotteryPiggyBank").name("piggy bank", "pottery piggy banks", "A piggy bank where you can deposit your hard earned coins.").itemTypes(new short[]{108, 30, 123, 195, 194, 52, 92, 48, 1}).imageNumber((short)0).decayTime(Long.MAX_VALUE).containerSize(20, 20, 20).modelName("model.creature.quadraped.pig.").difficulty(5.0f).material((byte)19).weightGrams(500).behaviourType((short)1).build();
            piggyBankTemplateId = piggyBankPotteryTemplate.getTemplateId();
            CreationEntryCreator.createSimpleEntry(1011, 14, 130, piggyBankClayTemplate.getTemplateId(), false, true, 0.0F, false, false, CreationCategories.POTTERY);
            TempStates.addState(new TempState(piggyBankClayTemplate.getTemplateId(), piggyBankPotteryTemplate.getTemplateId(), (short)10000, true, false, false));
        } catch (IOException e) { logger.warning(e.getMessage()); }
        /*
        AdvancedCreationEntry piggyBank = CreationEntryCreator.createAdvancedEntry(10016, 215, 214, decorations.startid + 2, false, true, 0.0F, false, false, CreationCategories.TOYS);
        piggyBank.addRequirement(new CreationRequirement(1, 213, 5, true));
        piggyBank.addRequirement(new CreationRequirement(2, 144, 5, true));
        piggyBank.addRequirement(new CreationRequirement(3, 204, 3, true));*/

    }
    @Override
    public void preInit(){
        // inject code for piggy bank
        // public final boolean moveToItem(Creature mover, long targetId, boolean lastMove) throws NoSuchItemException, NoSuchPlayerException, NoSuchCreatureException
        try {
            logger.info("injecting piggy bank");
            ClassPool classPool = HookManager.getInstance().getClassPool();
            CtClass ctItem = classPool.getCtClass("com.wurmonline.server.items.Item");
            CtMethod moveToItem = ctItem.getDeclaredMethod("moveToItem");
            moveToItem.insertBefore("{if(this.isCoin() && com.wurmonline.server.Items.getItem(targetId).getTemplateId() == org.tyoda.wurmunlimited.mods.CommonTweaks.CommonTweaks.getPiggyBankTemplateId()){return org.tyoda.wurmunlimited.mods.CommonTweaks.actions.PutInPiggyBankAction.depositCoin(mover, this, com.wurmonline.server.Items.getItem(targetId));}}");
            // org.tyoda.wurmunlimited.mods.CommonTweaks.CommonTweaks.logger.info("Checking for item moving");
            logger.info("successfully injected piggy bank");
        }catch(NotFoundException e){ e.printStackTrace(); }
        catch(javassist.CannotCompileException e){
            logger.severe("Could not compile bytecode injection");
            throw new RuntimeException(e);
        }
    }
    @Override
    public void init(){}
    @Override
    public void onServerStarted() {
        ModActions.init();
        ModActions.registerAction(new PutInPiggyBankAction());
        ModActions.registerAction(new BreakPiggyBankAction());
    }
    public static CommonTweaks getInstance(){
        return instance;
    }
    public String getVersion(){
        return version;
    }

    public static int getPiggyBankTemplateId() {
        return piggyBankTemplateId;
    }
    // TODO: add life preserver
    // TODO: add digtocorner action
    // TODO: piggy bank
    // TODO: life preserver
    // TODO: rare pottery planters give better ql?
}
