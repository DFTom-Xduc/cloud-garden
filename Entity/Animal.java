package Entity;
/** Eggs repeat; collecting meat consumes the animal, requiring a new purchase. */
public abstract class Animal {
    private boolean fed;
    private double progress;
    public abstract String id();
    public abstract String name(boolean english);
    public abstract String food(boolean english);
    public abstract BuildingType habitat();
    public abstract ProductType product();
    public abstract int purchasePrice();
    public abstract int feedPrice();
    public abstract int duration();
    public abstract int quantity();
    public boolean consumedOnCollection() { return product()!=ProductType.EGG; }
    public boolean isFed() { return fed; }
    public boolean isReady() { return fed && progress>=duration(); }
    public int secondsLeft() { return (int)Math.ceil(Math.max(0,duration()-progress)); }
    public double fraction() { return progress/duration(); }
    public void feed() { if(!fed) { fed=true; progress=0; } }
    public void advance(double seconds) { if(fed) progress=Math.min(duration(),progress+seconds); }
    public void reset() { fed=false; progress=0; }
    public void restore(boolean fed,double progress) { this.fed=fed;this.progress=Math.max(0,Math.min(duration(),progress)); }
    public static Animal create(String id) {
        return switch(id) { case "ga" -> new Ga(); case "lon" -> new Lon(); case "bo" -> new Bo(); default -> throw new IllegalArgumentException("Unknown animal: "+id); };
    }
}
