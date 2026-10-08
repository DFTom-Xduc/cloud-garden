package Service;

import Entity.*;
import java.util.*;
import static Service.Json.object;

/** Single authoritative game. Synchronization prevents double purchases/harvests from concurrent tabs. */
public final class GameService {
    private final GameState game;
    private long revision;
    public GameService(GameState game){this.game=game;}
    public synchronized void tick(double seconds) {
        GameSave before=game.snapshot();
        try{game.advanceSeconds(seconds);revision++;}catch(RuntimeException failure){game.restore(before);throw failure;}
    }
    public synchronized Map<String,Object> state() {
        List<Object> plots=new ArrayList<>(),flowers=new ArrayList<>(),buildings=new ArrayList<>(),animals=new ArrayList<>(),stock=new ArrayList<>();
        for(PlantPot p:game.pots)plots.add(object("row",p.getRow(),"column",p.getColumn(),"flowerId",p.isEmpty()?null:p.getFlower().id(),"level",p.getLevel(),"water",p.getWater(),"cooldown",p.getCooldown(),"power",p.getPower(),"salePrice",p.getSalePrice()));
        for(FlowerType f:FlowerCatalog.ALL)flowers.add(object("id",f.id(),"vi",f.vietnamese(),"en",f.english(),"seedPrice",f.seedPrice(),"salePrice",f.salePrice(),"basePower",f.basePower(),"color",f.color()));
        for(BuildingType b:BuildingType.values())buildings.add(object("id",b.name(),"vi",b.vi,"en",b.en,"price",b.price,"owned",game.buildings.contains(b),"unlocked",b!=BuildingType.COTTAGE||game.expanded));
        for(String id:List.of("ga","lon","bo")) {
            Animal type=Animal.create(id),animal=game.animals.get(id);
            animals.add(object("id",id,"vi",type.name(false),"en",type.name(true),"foodVi",type.food(false),"foodEn",type.food(true),"habitat",type.habitat().name(),"product",type.product().name(),"purchasePrice",type.purchasePrice(),"feedPrice",type.feedPrice(),"duration",type.duration(),"quantity",type.quantity(),"productPrice",type.product().price,"habitatBuilt",game.buildings.contains(type.habitat()),"exists",animal!=null,"fed",animal!=null&&animal.isFed(),"ready",animal!=null&&animal.isReady(),"secondsLeft",animal==null?type.duration():animal.secondsLeft(),"fraction",animal==null?0:animal.fraction(),"consumedOnCollection",type.consumedOnCollection()));
        }
        for(ProductType p:ProductType.values())stock.add(object("id",p.name(),"vi",p.vi,"en",p.en,"price",p.price,"quantity",game.inventory.get(p)));
        return object("revision",revision,"coins",game.coins,"diamonds",game.diamonds,"music",game.music,"sfx",game.sfx,"english",game.english,"expanded",game.expanded,"expansionPrice",GameState.EXPANSION_PRICE,"rewardClaimed",game.rewardClaimed,"harvestTotal",game.harvestedCount(),"day",game.day(),"clock",game.clockText(),"night",game.isNight(),"plots",plots,"flowers",flowers,"buildings",buildings,"animals",animals,"inventory",stock,"stockValue",game.stockValue());
    }
    public synchronized Map<String,Object> act(Map<String,String> input) {
        GameSave before=game.snapshot();String message;
        try {
            String action=input.getOrDefault("action","");
            switch(action) {
                case "plant" -> {
                    FlowerType f=FlowerCatalog.ALL.stream().filter(item->item.id().equals(required(input,"flowerId"))).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown flower"));
                    if(!game.plant(plot(input),f))conflict("Ô đất phải trống và bạn cần đủ coin mua hạt.","The plot must be empty and you need enough coins for seeds.");
                    message=game.text("Đã trồng ","Planted ")+f.name(game.english);
                }
                case "water" -> {
                    PlantPot p=plot(input);
                    if(!game.water(p))conflict("Hoa cần chờ 4 giây giữa hai lần tưới, tối đa cấp 3.","Wait 4 seconds between waterings. Flowers reach a maximum of level 3.");
                    message=game.text("Đã tưới · Cấp ","Watered · Level ")+p.getLevel()+game.text(" · Sức sống "," · Power ")+p.getPower();
                }
                case "dig" -> {if(!game.dig(plot(input)))conflict("Ô này đã trống.","This plot is already empty.");message=game.text("Đã đào bỏ hoa. Bạn có thể trồng lại.","Flower removed. You can replant this plot.");}
                case "harvest" -> {PlantPot p=plot(input);int value=p.getSalePrice();if(!game.harvest(p))conflict("Ô này chưa có hoa.","This plot has no flower.");message=game.text("Đã thu hoạch · +","Harvested · +")+value+" coin";}
                case "expand" -> {if(!game.expand())conflict("Bạn cần đủ coin và đảo chưa được mở rộng.","You need enough coins and an island that has not been expanded.");message=game.text("Đã mở rộng đảo: thêm 4 ô trồng!","Island expanded: four new plots!");}
                case "build" -> {BuildingType b=BuildingType.valueOf(required(input,"building"));if(!game.build(b))conflict("Chưa đủ coin, thiếu đất hoặc công trình đã có.","Not enough coins, more land required, or the building is already owned.");message=game.text("Đã xây ","Built ")+b.name(game.english);}
                case "buyAnimal" -> {if(!game.buyAnimal(required(input,"animal")))conflict("Cần có chuồng phù hợp, ô nuôi trống và đủ coin.","You need the right enclosure, an empty slot and enough coins.");message=game.text("Đã mua vật nuôi.","Animal purchased.");}
                case "feedAnimal" -> {if(!game.feedAnimal(required(input,"animal")))conflict("Vật nuôi đang được cho ăn hoặc chưa đủ coin.","This animal is already fed or you need more coins.");message=game.text("Đã mua thức ăn và cho ăn.","Feed purchased and served.");}
                case "collectAnimal" -> {if(!game.collectAnimal(required(input,"animal")))conflict("Vật nuôi chưa sẵn sàng.","This animal is not ready yet.");message=game.text("Đã chuyển sản phẩm vào kho.","Products added to inventory.");}
                case "sell" -> {int value=game.sellProducts();message=game.text("Đã bán sản phẩm · +","Products sold · +")+value+" coin";}
                case "claim" -> {if(!game.claimReward())conflict("Cần thu hoạch 6 ô và chỉ nhận thưởng một lần.","Harvest six plots. The reward can only be claimed once.");message=game.text("Đã nhận 120 coin và 1 kim cương!","Received 120 coins and one diamond!");}
                case "daynight" -> {game.toggleDayNight();message=game.text("Đã đổi thời gian trong ngày.","Time of day changed.");}
                case "settings" -> {
                    int music=number(input,"music"),sfx=number(input,"sfx");String lang=required(input,"language");
                    if(music<0||music>100||sfx<0||sfx>100||!Set.of("vi","en").contains(lang))throw new IllegalArgumentException("Invalid settings");
                    game.music=music;game.sfx=sfx;game.english=lang.equals("en");game.save();message=game.text("Đã lưu cài đặt.","Settings saved.");
                }
                default -> throw new IllegalArgumentException("Unknown action");
            }
            revision++;return object("state",state(),"message",message);
        } catch(RuntimeException error){game.restore(before);throw error;}
    }
    private PlantPot plot(Map<String,String> input) {
        int row=number(input,"row"),column=number(input,"column");
        return game.pots.stream().filter(p->p.getRow()==row&&p.getColumn()==column).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown plot"));
    }
    private static String required(Map<String,String> input,String key) {String value=input.get(key);if(value==null||value.isBlank())throw new IllegalArgumentException("Missing "+key);return value;}
    private static int number(Map<String,String> input,String key){return Integer.parseInt(required(input,key));}
    private void conflict(String vi,String en){throw new Conflict(game.text(vi,en));}
    public synchronized void save(){game.flushSave();}
    public static final class Conflict extends RuntimeException {public Conflict(String message){super(message);}}
}
