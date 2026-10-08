package Entity;
public class Lon extends Animal {
    public String id() { return "lon"; }
    public String name(boolean en) { return en?"Pig":"Lợn"; }
    public String food(boolean en) { return en?"Pig feed":"Cám lợn"; }
    public BuildingType habitat() { return BuildingType.MUD_PEN; }
    public ProductType product() { return ProductType.PORK; }
    public int purchasePrice() { return 45; }
    public int feedPrice() { return 8; }
    public int duration() { return 45; }
    public int quantity() { return 4; }
}
