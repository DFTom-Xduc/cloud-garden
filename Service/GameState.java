package Service;

import Entity.*;
import Repository.GameRepository;

import java.util.*;
import java.util.prefs.Preferences;

/** Domain rules and migration of the original local save; UI never deducts money directly. */
public class GameState {
    public static final int EXPANSION_PRICE = 260;
    public final List<PlantPot> pots = new ArrayList<>();
    public final Set<BuildingType> buildings = EnumSet.noneOf(BuildingType.class);
    public final Map<String,Animal> animals = new LinkedHashMap<>();
    public final Map<ProductType,Integer> inventory = new EnumMap<>(ProductType.class);
    public int coins=320, diamonds=5, music=35, sfx=65;
    public boolean english, rewardClaimed, expanded;
    public ChoCanhGac guardDog;
    public final Map<String,Integer> questProgress=new HashMap<>();
    public final Set<String> claimedQuests=new HashSet<>();
    private int harvestTotal;
    private double gameMinutes=480, sinceSave;
    private final Preferences preferences;
    private GameRepository repository;

    public GameState() { this(true); }
    public GameState(boolean persistent) { this(persistent?openStore():null); }
    public GameState(GameRepository repository,boolean migrateLegacy) {
        this((Preferences)null);
        this.repository=Objects.requireNonNull(repository);
        var stored=repository.load();
        if(stored.isPresent())restore(stored.get());
        else {
            if(migrateLegacy)restore(new GameState(openStore()).snapshot());
            save();
        }
    }
    public GameState(Preferences store) {
        preferences=store;
        if(store!=null) {
            coins=Math.max(0,store.getInt("coins",320)); diamonds=Math.max(0,store.getInt("diamonds",5));
            music=Math.max(0,Math.min(100,store.getInt("music",35))); sfx=Math.max(0,Math.min(100,store.getInt("sfx",65)));
            english=store.getBoolean("english",false);rewardClaimed=store.getBoolean("rewardClaimed",false);
            guardDog=store.getBoolean("guardDog",false)?new ChoCanhGac():null;expanded=store.getBoolean("expanded",false);gameMinutes=Math.max(0,store.getDouble("gameMinutes",480));
        }
        if(store!=null) {
            for(Quest q:QuestCatalog.ALL) {questProgress.put(q.metric(),Math.max(0,store.getInt("quest.progress."+q.metric(),0)));if(store.getBoolean("quest.claimed."+q.id(),false))claimedQuests.add(q.id());}
        }
        boolean current=store!=null&&store.getInt("schema",0)>=3;
        for(int row=0;row<3;row++) for(int col=0;col<4;col++) addPlot(row,col,current);
        if(expanded) for(int row=0;row<2;row++) for(int col=4;col<6;col++) addPlot(row,col,current);
        if(current) harvestTotal=Math.max(0,store.getInt("harvestTotal",0));
        for(BuildingType building:BuildingType.values())
            if(store==null?building==BuildingType.COOP:store.getBoolean("building."+building.name(),building==BuildingType.COOP)) buildings.add(building);
        for(String id:Animal.IDS) {
            if(store==null?id.equals("ga"):store.getBoolean("animal."+id+".exists",id.equals("ga"))) {
                Animal animal=Animal.create(id);
                if(buildings.contains(animal.habitat())) {
                    if(store!=null) animal.restore(store.getBoolean("animal."+id+".fed",false),store.getDouble("animal."+id+".progress",0));
                    animals.put(id,animal);
                }
            }
        }
        for(ProductType product:ProductType.values()) inventory.put(product,store==null?0:Math.max(0,store.getInt("stock."+product.name(),0)));
    }
    private static Preferences openStore() {
        try { return Preferences.userRoot().node("cloudgarden/farm-v2"); } catch(SecurityException ignored) { return null; }
    }
    private void addPlot(int row,int col,boolean current) {
        PlantPot pot=new PlantPot(row,col);String key="plot."+row+"."+col;
        if(current) {
            String id=preferences.get(key+".flower","");
            if(!id.isEmpty()) pot.restore(FlowerCatalog.find(id),preferences.getInt(key+".level",1),preferences.getInt(key+".water",0),preferences.getDouble(key+".cooldown",0));
        } else if(preferences!=null&&preferences.getBoolean(key,false)) { harvestTotal++; }
        else if(col<4) pot.plant(FlowerCatalog.ALL.get(col%FlowerCatalog.ALL.size()));
        pots.add(pot);
    }
    public String text(String vi,String en) { return english?en:vi; }
    public int harvestedCount() { return harvestTotal; }
    public boolean harvest(PlantPot pot) {
        if(pot==null||pot.isEmpty()||!pots.contains(pot)) return false;
        coins+=pot.getSalePrice();pot.clear();harvestTotal++;save();return true;
    }
    public boolean plant(PlantPot pot,FlowerType flower) {
        if(pot==null||!pots.contains(pot)||!pot.isEmpty()||flower==null||!FlowerCatalog.ALL.contains(flower)||coins<flower.seedPrice()) return false;
        coins-=flower.seedPrice();pot.plant(flower);increment("plant",1);save();return true;
    }
    public boolean dig(PlantPot pot) {
        if(pot==null||pot.isEmpty()||!pots.contains(pot))return false;
        pot.clear();save();return true;
    }
    public boolean water(PlantPot pot) {
        if(pot==null||!pots.contains(pot))return false;
        int previous=pot.getLevel();if(!pot.water())return false;
        increment("water",1);if(previous<3&&pot.getLevel()==3)increment("level3",1);save();return true;
    }
    private void increment(String metric,int amount) {questProgress.merge(metric,amount,Integer::sum);}
    public int questCount(Quest quest) {
        return switch(quest.metric()) {
            case "harvest" -> harvestTotal;
            case "expanded" -> expanded?1:0;
            case "cottage" -> buildings.contains(BuildingType.COTTAGE)?1:0;
            case "habitats" -> (int)buildings.stream().filter(b->Set.of(BuildingType.COOP,BuildingType.MUD_PEN,BuildingType.COW_BARN).contains(b)).count();
            case "hill_habitats" -> (int)buildings.stream().filter(b->b==BuildingType.SHEEP_BARN||b==BuildingType.GOAT_BARN).count();
            case "guard_dog" -> guardDog!=null?1:0;
            default -> questProgress.getOrDefault(quest.metric(),0);
        };
    }
    public boolean questClaimed(Quest quest) {return claimedQuests.contains(quest.id())||quest.id().equals("first_harvest")&&rewardClaimed;}
    public boolean claimQuest(String id) {
        Quest quest=QuestCatalog.find(id);
        if(questClaimed(quest)||questCount(quest)<quest.target())return false;
        coins+=quest.coins();diamonds+=quest.diamonds();claimedQuests.add(id);
        if(id.equals("first_harvest"))rewardClaimed=true;
        save();return true;
    }
    public boolean claimReward() {return claimQuest("first_harvest");}
    public boolean expand() {
        if(expanded||coins<EXPANSION_PRICE)return false;
        coins-=EXPANSION_PRICE;expanded=true;
        for(int row=0;row<2;row++)for(int col=4;col<6;col++)pots.add(new PlantPot(row,col));
        save();return true;
    }
    public boolean build(BuildingType type) {
        if(type==null||buildings.contains(type)||coins<type.price||type.requiresExpansion()&&!expanded)return false;
        coins-=type.price;buildings.add(type);save();return true;
    }
    public boolean buyAnimal(String id) {
        Animal animal=Animal.create(id);
        if(animals.containsKey(id)||!buildings.contains(animal.habitat())||coins<animal.purchasePrice())return false;
        coins-=animal.purchasePrice();animals.put(id,animal);save();return true;
    }
    public boolean buyGuardDog() {
        if(guardDog!=null||coins<ChoCanhGac.PRICE||(!buildings.contains(BuildingType.SHEEP_BARN)&&!buildings.contains(BuildingType.GOAT_BARN)))return false;
        coins-=ChoCanhGac.PRICE;guardDog=new ChoCanhGac();save();return true;
    }
    public double gameMinutes() {return gameMinutes;}
    public boolean feedAnimal(String id) {
        Animal animal=animals.get(id);
        if(animal==null||animal.isFed()||coins<animal.feedPrice())return false;
        coins-=animal.feedPrice();animal.feed();increment("feed_"+id,1);save();return true;
    }
    public boolean collectAnimal(String id) {
        Animal animal=animals.get(id);
        if(animal==null||!animal.isReady())return false;
        inventory.merge(animal.product(),animal.quantity(),Integer::sum);increment("collect_"+animal.product().name(),animal.quantity());
        if(animal.consumedOnCollection())animals.remove(id);else animal.reset();
        save();return true;
    }
    public int stockValue() { return inventory.entrySet().stream().mapToInt(e->e.getKey().price*e.getValue()).sum(); }
    public int sellProducts() {
        int value=stockValue();coins+=value;if(value>0)increment("sold",value);inventory.replaceAll((key,count)->0);save();return value;
    }
    public int day() { return (int)(gameMinutes/1440)+1; }
    public int hour() { return (int)(gameMinutes%1440)/60; }
    public boolean isNight() { return hour()<6||hour()>=18; }
    public String clockText() { return String.format("%02d:%02d",hour(),(int)gameMinutes%60); }
    public void toggleDayNight() {
        double start=Math.floor(gameMinutes/1440)*1440;
        gameMinutes=isNight()?(hour()<6?start+360:start+1800):start+1080;
        save();
    }
    public void advanceSeconds(double seconds) {
        if(!Double.isFinite(seconds)||seconds<=0)return;
        gameMinutes+=seconds*10;
        for(PlantPot pot:pots)pot.advance(seconds);
        for(Animal animal:animals.values())animal.advance(seconds);
        sinceSave+=seconds;if(sinceSave>=5){save();sinceSave=0;}
    }
    public void flushSave() {
        save();
        if(preferences!=null)try{preferences.flush();}catch(java.util.prefs.BackingStoreException | SecurityException ignored){}
    }
    public void save() {
        if(repository!=null){repository.save(snapshot());return;}
        if(preferences==null)return;
        try {
            preferences.putInt("schema",3);preferences.putInt("coins",coins);preferences.putInt("diamonds",diamonds);
            preferences.putInt("music",music);preferences.putInt("sfx",sfx);preferences.putBoolean("english",english);
            preferences.putBoolean("rewardClaimed",rewardClaimed);preferences.putInt("harvestTotal",harvestTotal);
            preferences.putBoolean("guardDog",guardDog!=null);preferences.putBoolean("expanded",expanded);preferences.putDouble("gameMinutes",gameMinutes);
            for(var entry:questProgress.entrySet())preferences.putInt("quest.progress."+entry.getKey(),entry.getValue());
            for(Quest q:QuestCatalog.ALL)preferences.putBoolean("quest.claimed."+q.id(),claimedQuests.contains(q.id()));
            for(PlantPot pot:pots) {
                String key="plot."+pot.getRow()+"."+pot.getColumn();
                preferences.put(key+".flower",pot.isEmpty()?"":pot.getFlower().id());preferences.putInt(key+".level",pot.getLevel());
                preferences.putInt(key+".water",pot.getWater());preferences.putDouble(key+".cooldown",pot.getCooldown());
            }
            for(BuildingType b:BuildingType.values())preferences.putBoolean("building."+b.name(),buildings.contains(b));
            for(String id:Animal.IDS) {
                Animal animal=animals.get(id);String key="animal."+id;
                preferences.putBoolean(key+".exists",animal!=null);
                if(animal!=null){preferences.putBoolean(key+".fed",animal.isFed());preferences.putDouble(key+".progress",animal.fraction()*animal.duration());}
            }
            for(var entry:inventory.entrySet())preferences.putInt("stock."+entry.getKey().name(),entry.getValue());
        } catch(SecurityException ignored) { /* Session-only fallback. */ }
    }
    public GameSave snapshot() {
        List<GameSave.Plot> plots=pots.stream().map(p->new GameSave.Plot(p.getRow(),p.getColumn(),p.isEmpty()?null:p.getFlower().id(),p.getLevel(),p.getWater(),p.cooldownSeconds())).toList();
        List<GameSave.Livestock> livestock=animals.values().stream().map(a->new GameSave.Livestock(a.id(),a.isFed(),a.fraction()*a.duration())).toList();
        return new GameSave(coins,diamonds,music,sfx,english,rewardClaimed,expanded,harvestTotal,gameMinutes,plots,buildings,livestock,inventory,questProgress,claimedQuests,guardDog!=null);
    }
    public void restore(GameSave save) {
        guardDog=save.guardDog()?new ChoCanhGac():null;coins=save.coins();diamonds=save.diamonds();music=save.music();sfx=save.sfx();english=save.english();
        rewardClaimed=save.rewardClaimed();expanded=save.expanded();harvestTotal=save.harvestTotal();gameMinutes=save.gameMinutes();
        questProgress.clear();questProgress.putAll(save.questProgress());claimedQuests.clear();claimedQuests.addAll(save.claimedQuests());
        pots.clear();
        for(var plot:save.plots()) {
            PlantPot p=new PlantPot(plot.row(),plot.column());
            p.restore(plot.flowerId()==null?null:FlowerCatalog.find(plot.flowerId()),plot.level(),plot.water(),plot.cooldown());pots.add(p);
        }
        buildings.clear();buildings.addAll(save.buildings());animals.clear();
        for(var animal:save.animals()){Animal a=Animal.create(animal.id());a.restore(animal.fed(),animal.progress());animals.put(a.id(),a);}
        inventory.clear();for(ProductType p:ProductType.values())inventory.put(p,save.inventory().getOrDefault(p,0));
    }
}
