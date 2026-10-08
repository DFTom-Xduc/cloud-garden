package tests;
import Entity.*;
import Service.*;
import java.util.*;
import java.util.prefs.AbstractPreferences;

public class FarmSmokeTest {
    private static void check(boolean pass,String message) { if(!pass)throw new AssertionError(message); }
    private static void rules() {
        check(FlowerCatalog.ALL.size()>=4,"Four demo flowers are available");
        GameState s=new GameState(false);PlantPot pot=s.pots.get(0);int original=s.coins;
        check(!s.plant(pot,FlowerCatalog.ALL.get(1))&&s.coins==original,"Cannot plant over a flower");
        check(s.water(pot)&&!s.water(pot),"Watering cooldown prevents repeated clicks");
        s.advanceSeconds(4);check(s.water(pot)&&pot.getLevel()==2,"Two waterings evolve a flower");
        s.advanceSeconds(4);s.water(pot);s.advanceSeconds(4);s.water(pot);
        check(pot.getLevel()==3&&pot.getPower()==pot.getFlower().basePower()*3,"Level-three strength");
        s.advanceSeconds(4);check(!s.water(pot),"Maximum flower level enforced");
        int price=pot.getSalePrice();check(s.harvest(pot)&&s.coins==original+price&&!s.harvest(pot),"Sale price follows level and is awarded once");
        check(s.plant(pot,FlowerCatalog.ALL.get(2)),"Can replant a harvested plot");
        int harvests=s.harvestedCount(),beforeDig=s.coins;
        check(s.dig(pot)&&!s.dig(pot)&&s.harvestedCount()==harvests&&s.coins==beforeDig,"Digging does not grant rewards");
        s.coins=0;check(!s.plant(pot,FlowerCatalog.ALL.get(0))&&!s.expand()&&!s.build(BuildingType.MUD_PEN),"Unaffordable actions do not mutate state");
        s.coins=2000;check(!s.build(BuildingType.COTTAGE),"New house requires expanded land");
        check(s.expand()&&s.pots.size()==16&&!s.expand(),"Expansion adds four plots once");
        check(s.build(BuildingType.COTTAGE)&&!s.build(BuildingType.COTTAGE),"House charged once");
        check(!s.buyAnimal("lon")&&!s.buyAnimal("bo"),"Animals require appropriate habitats");
        check(s.build(BuildingType.MUD_PEN)&&s.build(BuildingType.COW_BARN),"Build mud pen and cattle barn");
        check(s.buyAnimal("lon")&&s.buyAnimal("bo")&&!s.buyAnimal("ga"),"One animal per habitat");
        beforeDig=s.coins;
        check(s.feedAnimal("bo")&&s.coins==beforeDig-12&&!s.feedAnimal("bo"),"Commercial grass purchased once per cow cycle");
        s.feedAnimal("ga");s.feedAnimal("lon");check(!s.collectAnimal("ga"),"Products unavailable before timer completes");
        s.advanceSeconds(60);
        check(s.collectAnimal("ga")&&s.collectAnimal("lon")&&s.collectAnimal("bo"),"All animal products collect");
        check(s.animals.containsKey("ga")&&!s.animals.containsKey("lon")&&!s.animals.containsKey("bo"),"Eggs preserve chicken; meat consumes pig and cow");
        check(!s.collectAnimal("ga")&&!s.collectAnimal("lon"),"No duplicate product collection");
        check(s.inventory.get(ProductType.EGG)==3&&s.inventory.get(ProductType.PORK)==4&&s.inventory.get(ProductType.BEEF)==5,"Correct product inventory");
        int value=3*8+4*28+5*40,coins=s.coins;
        check(s.sellProducts()==value&&s.coins==coins+value&&s.sellProducts()==0,"Products sold once at catalog prices");
        check(s.feedAnimal("ga"),"Chicken can produce another batch");
        boolean night=s.isNight();s.toggleDayNight();check(s.isNight()!=night,"Day/night toggle");
        for(int i=0;s.harvestedCount()<6;i++)s.harvest(s.pots.get(i+1));
        coins=s.coins;check(s.claimReward()&&!s.claimReward()&&s.coins==coins+120&&s.diamonds==6,"Cumulative quest rewarded once");
        GameState clock=new GameState(false);clock.advanceSeconds(60);check(clock.isNight(),"Automatic clock reaches evening");
        clock.advanceSeconds(72);check(!clock.isNight()&&clock.day()==2,"Automatic dawn advances calendar day");
    }
    private static void saves() {
        MemoryPreferences memory=new MemoryPreferences();memory.putInt("coins",777);memory.putInt("diamonds",12);
        memory.putBoolean("english",true);memory.putBoolean("plot.0.0",true);
        GameState migrated=new GameState(memory);
        check(migrated.coins==777&&migrated.diamonds==12&&migrated.english&&migrated.pots.get(0).isEmpty()&&migrated.harvestedCount()==1,"Old save migrated without resetting currency or harvested plots");
        migrated.expand();migrated.build(BuildingType.MUD_PEN);migrated.buyAnimal("lon");migrated.feedAnimal("lon");
        PlantPot p=migrated.pots.get(1);migrated.water(p);migrated.advanceSeconds(4);migrated.water(p);migrated.advanceSeconds(4);
        migrated.feedAnimal("ga");migrated.advanceSeconds(30);migrated.collectAnimal("ga");migrated.toggleDayNight();migrated.save();
        GameState loaded=new GameState(memory);
        check(loaded.coins==migrated.coins&&loaded.pots.size()==16&&loaded.pots.get(1).getLevel()==2,"Save reload keeps currency, expanded plots and flower level");
        check(loaded.buildings.contains(BuildingType.MUD_PEN)&&loaded.animals.get("lon").isFed()&&loaded.inventory.get(ProductType.EGG)==3,"Save reload preserves livestock, feed progress and inventory");
        check(loaded.isNight()==migrated.isNight()&&loaded.harvestedCount()==1,"Clock and cumulative quest persist");
    }
    public static void main(String[] args) { rules(); saves(); System.out.println("PASS: domain rules and legacy save migration"); }
    private static class MemoryPreferences extends AbstractPreferences {
        private final Map<String,String> values=new HashMap<>();
        MemoryPreferences(){super(null,"");}
        protected void putSpi(String k,String v){values.put(k,v);}
        protected String getSpi(String k){return values.get(k);}
        protected void removeSpi(String k){values.remove(k);}
        protected void removeNodeSpi(){values.clear();}
        protected String[] keysSpi(){return values.keySet().toArray(String[]::new);}
        protected String[] childrenNamesSpi(){return new String[0];}
        protected AbstractPreferences childSpi(String name){throw new UnsupportedOperationException();}
        protected void syncSpi(){}
        protected void flushSpi(){}
    }
}
