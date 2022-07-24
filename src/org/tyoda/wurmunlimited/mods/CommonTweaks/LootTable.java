package org.tyoda.wurmunlimited.mods.CommonTweaks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LootTable {
    public static final Random random = new Random();
    public final HashMap<Integer, ArrayList<Integer>> lootTable = new HashMap<>();
    public LootTable(HashMap<Integer, ArrayList<Integer>> newLootTable){
        for (Integer i : newLootTable.keySet()) {
            ArrayList<Integer> arr = new ArrayList<>(newLootTable.get(i));
            this.lootTable.put(i, arr);
        }
    }
    public ArrayList<Integer> getLoot(int maxWeight){
        ArrayList<Integer> loot = new ArrayList<>();
        int lootValue = 0;
        while(lootValue < maxWeight){
            int diff = maxWeight-lootValue;
            Integer[] weights = new Integer[lootTable.size()];
            weights = lootTable.keySet().toArray(weights);
            int weight;
            int tries = 0;
            while((weight = weights[random.nextInt(weights.length)]) > diff && tries++ < 1000);
            if(tries == 1000) break;

            loot.add(lootTable.get(weight).get(random.nextInt(lootTable.get(weight).size())));
            lootValue += weight;
        }
        return loot;
    }

    public static ArrayList<Integer> generateCoins(int moneyPool){
        int currentItemTemplate = 61; // gold-twenty coins
        int currentSteps = 20000000;
        ArrayList<Integer> coins = new ArrayList<>(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 57; // gold-five coins
        currentSteps = 5000000;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 53; // gold coins
        currentSteps = 1000000;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 60; // silver-twenty coins
        currentSteps = 200000;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 56; // silver-five coins
        currentSteps = 50000;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 52; // silver coins
        currentSteps = 10000;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 58; // copper-twenty coins
        currentSteps = 2000;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 54; // copper-five coins
        currentSteps = 500;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 50; // copper coins
        currentSteps = 100;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 59; // iron-twenty coins
        currentSteps = 20;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 55; // iron-five coins
        currentSteps = 5;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        moneyPool %= currentSteps;
        currentItemTemplate = 51; // iron coins
        currentSteps = 1;
        coins.addAll(createCoins(currentItemTemplate, moneyPool, currentSteps));
        return coins;
    }
    private static ArrayList<Integer> createCoins(int templateId, int moneyPool, int steps){
        ArrayList<Integer> coins = new ArrayList<>();
        while(moneyPool >= steps){
            coins.add(templateId);
            moneyPool -= steps;
        }
        return coins;
    }
}
