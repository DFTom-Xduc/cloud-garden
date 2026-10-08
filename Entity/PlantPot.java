package Entity;

public class PlantPot {
    public static final int MAX_LEVEL = 3, WATER_PER_LEVEL = 2, WATER_COOLDOWN = 4;
    private final int row, column;
    private boolean selected;
    private FlowerType flower;
    private int level = 1, water;
    private double cooldown;
    public PlantPot(int row, int column) { this.row=row; this.column=column; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected=selected; }
    public boolean isHarvested() { return flower==null; }
    public boolean isEmpty() { return flower==null; }
    public FlowerType getFlower() { return flower; }
    public int getLevel() { return level; }
    public int getWater() { return water; }
    public int getCooldown() { return (int)Math.ceil(cooldown); }
    public double cooldownSeconds() { return cooldown; }
    public int getPower() { return flower==null?0:flower.basePower()*level; }
    public int getSalePrice() { return flower==null?0:flower.salePrice()+(level-1)*12; }
    public void plant(FlowerType type) { flower=type; level=1; water=0; cooldown=0; }
    public void clear() { flower=null; level=1; water=0; cooldown=0; }
    public boolean water() {
        if(isEmpty()||level>=MAX_LEVEL||cooldown>0) return false;
        cooldown=WATER_COOLDOWN;
        if(++water>=WATER_PER_LEVEL) { level++; water=0; }
        return true;
    }
    public void advance(double seconds) { cooldown=Math.max(0,cooldown-seconds); }
    public void restore(FlowerType type,int level,int water,double cooldown) {
        flower=type; this.level=Math.max(1,Math.min(MAX_LEVEL,level));
        this.water=Math.max(0,Math.min(WATER_PER_LEVEL-1,water));
        this.cooldown=Math.max(0,Math.min(WATER_COOLDOWN,cooldown));
    }
}
