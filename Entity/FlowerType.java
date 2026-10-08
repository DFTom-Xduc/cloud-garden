package Entity;

/** Catalog data shared by the flowers growing in each plot. */
public record FlowerType(String id, String vietnamese, String english, int seedPrice,
                         int salePrice, int basePower, String color) {
    public String name(boolean en) { return en ? english : vietnamese; }
    public String stageName(boolean en, int level) {
        return name(en) + (level == 3 ? (en ? " · Radiant" : " · Rực rỡ") : level == 2 ? (en ? " · Blooming" : " · Nở rộ") : "");
    }
}
