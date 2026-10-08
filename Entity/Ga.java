package Entity;
public class Ga extends Animal {
    public String id() { return "ga"; }
    public String name(boolean en) { return en?"Chicken":"Gà"; }
    public String food(boolean en) { return en?"Grain":"Hạt ngũ cốc"; }
    public BuildingType habitat() { return BuildingType.COOP; }
    public ProductType product() { return ProductType.EGG; }
    public int purchasePrice() { return 30; }
    public int feedPrice() { return 3; }
    public int duration() { return 30; }
    public int quantity() { return 3; }
}
