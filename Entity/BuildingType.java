package Entity;
public enum BuildingType {
    COOP("Chuồng gà","Chicken coop",100), MUD_PEN("Bãi bùn cho lợn","Pig mud pen",140),
    COW_BARN("Chuồng bò & bãi cỏ","Cattle barn & paddock",180), COTTAGE("Nhà gỗ mới","Timber farmhouse",200);
    public final String vi,en;
    public final int price;
    BuildingType(String vi,String en,int price) { this.vi=vi;this.en=en;this.price=price; }
    public String name(boolean english) { return english?en:vi; }
}
