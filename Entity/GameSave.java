package Entity;

import java.util.*;

/** Immutable snapshot exchanged between the domain service and database repository. */
public record GameSave(int coins, int diamonds, int music, int sfx, boolean english,
                       boolean rewardClaimed, boolean expanded, int harvestTotal, double gameMinutes,
                       List<Plot> plots, Set<BuildingType> buildings, List<Livestock> animals,
                       Map<ProductType,Integer> inventory) {
    public GameSave {
        plots=List.copyOf(plots);buildings=Set.copyOf(buildings);animals=List.copyOf(animals);inventory=Map.copyOf(inventory);
    }
    public record Plot(int row,int column,String flowerId,int level,int water,double cooldown) {}
    public record Livestock(String id,boolean fed,double progress) {}
}
