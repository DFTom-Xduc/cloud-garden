package Entity;
public enum ProductType {
    EGG("Trứng", "Eggs", 8), PORK("Thịt lợn", "Pork", 28), BEEF("Thịt bò", "Beef", 40),
    WOOL("Lông cừu", "Wool", 22), GOAT_MEAT("Thịt dê", "Goat meat", 38);
    public final String vi, en;
    public final int price;
    ProductType(String vi,String en,int price) { this.vi=vi; this.en=en; this.price=price; }
    public String name(boolean english) { return english?en:vi; }
}
