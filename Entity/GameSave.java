package Entity;

import java.util.*;

/** Immutable snapshot exchanged between the domain service and database repository. */
public record GameSave(int coins, int diamonds, int music, int sfx, boolean english,
                       boolean rewardClaimed, boolean expanded, int harvestTotal, double gameMinutes,
                       List<Plot> plots, Set<BuildingType> buildings, List<Livestock> animals,
                       Map<ProductType,Integer> inventory, Map<String,Integer> questProgress, Set<String> claimedQuests, boolean guardDog) {
    public GameSave {
        plots=List.copyOf(plots);buildings=Set.copyOf(buildings);animals=List.copyOf(animals);inventory=Map.copyOf(inventory);questProgress=Map.copyOf(questProgress);claimedQuests=Set.copyOf(claimedQuests);
    }
    public GameSave(int coins,int diamonds,int music,int sfx,boolean english,boolean rewardClaimed,boolean expanded,int harvestTotal,double gameMinutes,List<Plot> plots,Set<BuildingType> buildings,List<Livestock> animals,Map<ProductType,Integer> inventory) {
        this(coins,diamonds,music,sfx,english,rewardClaimed,expanded,harvestTotal,gameMinutes,plots,buildings,animals,inventory,Map.of(),Set.of());
    }
    public GameSave(int coins,int diamonds,int music,int sfx,boolean english,boolean rewardClaimed,boolean expanded,int harvestTotal,double gameMinutes,List<Plot> plots,Set<BuildingType> buildings,List<Livestock> animals,Map<ProductType,Integer> inventory,Map<String,Integer> questProgress,Set<String> claimedQuests) {
        this(coins,diamonds,music,sfx,english,rewardClaimed,expanded,harvestTotal,gameMinutes,plots,buildings,animals,inventory,questProgress,claimedQuests,false);
    }
    public record Plot(int row,int column,String flowerId,int level,int water,double cooldown) {}
    public record Livestock(String id,boolean fed,double progress) {}
}
