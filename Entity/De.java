package Entity;
public final class De extends Animal {
    public String id() {return "de";}
    public String name(boolean english) {return english?"Goat":"Dê";}
    public String food(boolean english) {return english?"Fresh grass and leaves":"Cỏ và lá";}
    public BuildingType habitat() {return BuildingType.GOAT_BARN;}
    public ProductType product() {return ProductType.GOAT_MEAT;}
    public int purchasePrice() {return 65;}
    public int feedPrice() {return 10;}
    public int duration() {return 55;}
    public int quantity() {return 5;}
    public boolean consumedOnCollection() {return true;}
}
