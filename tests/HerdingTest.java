package tests;
import Entity.*;
import Service.*;
import Repository.*;
import Controller.GameController;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import java.net.http.*;
import java.util.regex.*;

public final class HerdingTest {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    private static GameState at(GameState g,double minutes){GameSave s=g.snapshot();g.restore(new GameSave(s.coins(),s.diamonds(),s.music(),s.sfx(),s.english(),s.rewardClaimed(),s.expanded(),s.harvestTotal(),minutes,s.plots(),s.buildings(),s.animals(),s.inventory(),s.questProgress(),s.claimedQuests(),s.guardDog()));return g;}
    public static void main(String[] args) throws Exception {
        GameState g=new GameState(false);g.coins=5000;
        check(!g.build(BuildingType.SHEEP_BARN)&&!g.build(BuildingType.GOAT_BARN),"Both new farms require expansion");
        check(!g.buyGuardDog()&&!g.buyAnimal("cuu"),"Dog and sheep require a barn");
        check(g.expand()&&g.build(BuildingType.SHEEP_BARN)&&g.build(BuildingType.GOAT_BARN),"Expand and build both farms");
        check(g.buyAnimal("cuu")&&g.buyAnimal("de")&&g.buyGuardDog(),"New entities purchased");
        int wallet=g.coins;check(!g.buyGuardDog()&&!g.buyAnimal("cuu")&&g.coins==wallet,"Duplicate purchases never charged");
        check(ProductType.GOAT_MEAT.price==38&&ProductType.BEEF.price==40,"Goat meat is slightly below beef");
        for(int i=0;i<2;i++){check(g.feedAnimal("cuu"),"Feed sheep");g.advanceSeconds(50);check(g.collectAnimal("cuu")&&g.animals.containsKey("cuu"),"Shearing preserves sheep");}
        check(g.inventory.get(ProductType.WOOL)==8,"Two wool cycles");
        check(g.feedAnimal("de"),"Feed goat");g.advanceSeconds(55);check(g.collectAnimal("de")&&!g.animals.containsKey("de")&&g.inventory.get(ProductType.GOAT_MEAT)==5,"Meat consumes goat and stores five portions");
        g.buyAnimal("de");
        double[][] samples={{359,0},{360,0},{385,.5},{410,1},{720,1},{1079,1},{1080,1},{1105,.5},{1130,0},{1439,0},{1440+360,0},{1440+410,1}};
        for(double[] sample:samples){at(g,sample[0]);check(Math.abs((double)HerdingService.status(g).get("pastureFraction")-sample[1])<.000001,"Correct route at "+sample[0]);}
        check(HerdingService.status(at(g,360)).get("duty").equals("HERD_OUT"),"06:00 dog herds out");
        check(HerdingService.status(at(g,1080)).get("duty").equals("HERD_IN"),"18:00 dog herds home");
        check(HerdingService.status(at(g,720)).get("duty").equals("GUARD_GOATS"),"Dog guards goats between routes");
        check(HerdingService.status(at(g,1200)).get("mode").equals("INDOORS"),"Sheep indoors after sunset route");
        g.guardDog=null;check(HerdingService.status(at(g,720)).get("mode").equals("NEEDS_DOG")&&HerdingService.pastureFraction(720,false)==0,"No automatic herding without dog");g.guardDog=new ChoCanhGac();
        at(g,359);g.advanceSeconds(6);check(HerdingService.status(g).get("mode").equals("GRAZING"),"Skipped tick still reaches correct pasture state");
        check(g.questCount(QuestCatalog.find("habitats"))==1,"New barns do not fulfill old three-habitat quest");
        check(g.questCount(QuestCatalog.find("hill_farm"))==2&&g.claimQuest("wool")&&!g.claimQuest("wool"),"New farm and wool quests work once");
        migration(g);dogRollback();http();
        System.out.println("PASS: sheep wool, goat meat, guard dog, 06:00/18:00 routes, skipped ticks, old SQLite migration and HTTP");
    }
    private static void migration(GameState fixture) throws Exception {
        Path folder=Files.createTempDirectory("herd-migration-"),db=folder.resolve("old.db"),oldSchema=folder.resolve("old.sql");
        String old=Files.readString(Path.of("database/schema.sql")).replace("'ga','lon','bo','cuu','de'","'ga','lon','bo'").replace("'EGG','PORK','BEEF','WOOL','GOAT_MEAT'","'EGG','PORK','BEEF'").replaceAll("(?s)CREATE TABLE IF NOT EXISTS farm_settings.*?;","");Files.writeString(oldSchema,old);
        // Construct a real version-2 database, including data in the restricted tables.
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db);Statement s=c.createStatement()){
            for(String sql:old.split(";"))if(!sql.isBlank())s.execute(sql);
            s.execute("INSERT INTO player VALUES(1,777,9,35,65,0,0,0,0,1085)");s.execute("INSERT INTO animals VALUES(1,'ga',1,13)");s.execute("INSERT INTO inventory VALUES(1,'BEEF',7)");s.execute("INSERT INTO quest_claims VALUES(1,'eggs')");s.execute("PRAGMA user_version=2");
        }
        GameSave expected;
        try(var repo=new SqliteGameRepository(db,Path.of("database/schema.sql"))){
            GameState migrated=new GameState(repo,false);check(migrated.coins==777&&migrated.animals.get("ga").isFed()&&migrated.inventory.get(ProductType.BEEF)==7&&migrated.claimedQuests.contains("eggs"),"Migration preserves existing data");
            repo.save(fixture.snapshot());expected=repo.load().orElseThrow();
            var duplicate=new ArrayList<>(expected.plots());duplicate.add(expected.plots().get(0));
            GameSave broken=new GameSave(0,0,0,0,false,false,false,0,0,duplicate,expected.buildings(),expected.animals(),expected.inventory(),expected.questProgress(),expected.claimedQuests(),false);
            try{repo.save(broken);throw new AssertionError("Bad save should fail");}catch(IllegalStateException correct){}
            check(repo.load().orElseThrow().equals(expected),"SQL failure preserves dog, wool and wallet together");
        }
        try(var repo=new SqliteGameRepository(db,Path.of("database/schema.sql"))){GameState reload=new GameState(repo,false);check(reload.snapshot().equals(expected)&&reload.guardDog!=null,"New animals, dog, wool and clock survive cold reload");}
    }
    private static void dogRollback(){
        class Disk implements GameRepository {
            boolean fail;
            public Optional<GameSave> load(){return Optional.empty();}
            public void save(GameSave s){if(fail)throw new IllegalStateException("disk failure");}
            public void close(){}
        }
        Disk disk=new Disk();GameState g=new GameState(disk,false);g.coins=1000;g.expand();g.build(BuildingType.SHEEP_BARN);GameSave before=g.snapshot();disk.fail=true;
        try{new GameService(g).act(Map.of("action","buyGuardDog"));throw new AssertionError("Save must fail");}catch(IllegalStateException expected){}
        check(g.snapshot().equals(before)&&g.guardDog==null,"Failed purchase rolls back dog and wallet in memory");
    }
    private static void http() throws Exception {
        GameState g=new GameState(false);g.coins=5000;GameService service=new GameService(g);
        try(var controller=new GameController(service,Path.of("frontend"),0);var client=HttpClient.newHttpClient()){
            controller.start(false);String base="http://127.0.0.1:"+controller.port();
            String body=client.send(HttpRequest.newBuilder(URI.create(base+"/api/state")).GET().build(),HttpResponse.BodyHandlers.ofString()).body();
            Matcher m=Pattern.compile("\"token\":\"([^\"]+)\"").matcher(body);check(m.find(),"HTTP token");String token=m.group(1);
            check(post(client,base,token,"action=buyGuardDog")==409,"HTTP dog prerequisites");
            for(String action:List.of("action=expand","action=build&building=SHEEP_BARN","action=build&building=GOAT_BARN","action=buyAnimal&animal=cuu","action=buyAnimal&animal=de","action=buyGuardDog","action=feedAnimal&animal=cuu","action=feedAnimal&animal=de"))check(post(client,base,token,action)==200,"HTTP "+action);
            check(post(client,base,token,"action=buyGuardDog")==409,"HTTP duplicate dog");service.tick(55);
            check(post(client,base,token,"action=collectAnimal&animal=cuu")==200&&post(client,base,token,"action=collectAnimal&animal=de")==200,"HTTP new products");
            check(g.inventory.get(ProductType.WOOL)==4&&g.inventory.get(ProductType.GOAT_MEAT)==5,"HTTP product quantities");
        }
    }
    private static int post(HttpClient client,String base,String token,String body) throws Exception{return client.send(HttpRequest.newBuilder(URI.create(base+"/api/action")).header("X-Game-Token",token).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString()).statusCode();}
}
