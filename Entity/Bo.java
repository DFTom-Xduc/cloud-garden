package Entity;
public class Bo extends Animal {
    public String id() { return "bo"; }
    public String name(boolean en) { return en?"Cow":"Bò"; }
    public String food(boolean en) { return en?"Commercial grass feed":"Cỏ công nghiệp"; }
    public BuildingType habitat() { return BuildingType.COW_BARN; }
    public ProductType product() { return ProductType.BEEF; }
    public int purchasePrice() { return 70; }
    public int feedPrice() { return 12; }
    public int duration() { return 60; }
    public int quantity() { return 5; }
}
