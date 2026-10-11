package Entity;
public final class Cuu extends Animal {
    public String id() {return "cuu";}
    public String name(boolean english) {return english?"Sheep":"Cừu";}
    public String food(boolean english) {return english?"Fresh grass and leaves":"Cỏ non";}
    public BuildingType habitat() {return BuildingType.SHEEP_BARN;}
    public ProductType product() {return ProductType.WOOL;}
    public int purchasePrice() {return 65;}
    public int feedPrice() {return 9;}
    public int duration() {return 50;}
    public int quantity() {return 4;}
    public boolean consumedOnCollection() {return false;}
}
