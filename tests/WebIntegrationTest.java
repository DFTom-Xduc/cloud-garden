package tests;

import Entity.*;
import Controller.GameController;
import Repository.*;
import Service.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/** Real JDBC and HTTP tests; every database is isolated from the user's save. */
public final class WebIntegrationTest {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        FarmSmokeTest.main(args);QuestTest.main(args);HerdingTest.main(args);database();failedSave();http();
        System.out.println("PASS: SQLite cold reload, SQL rollback, service rollback, HTTP validation, concurrent harvest and static frontend.");
    }
    private static void database() throws Exception {
        Path file=Files.createTempDirectory("cloudgarden-db-test-").resolve("game.db"),schema=Path.of("database/schema.sql");
        GameSave expected;
        try(var repository=new SqliteGameRepository(file,schema)) {
            GameState game=new GameState(repository,false);game.coins=2000;
            game.expand();game.build(BuildingType.COTTAGE);game.build(BuildingType.MUD_PEN);game.buyAnimal("lon");game.feedAnimal("lon");
            game.water(game.pots.get(0));game.advanceSeconds(4);game.water(game.pots.get(0));
            game.feedAnimal("ga");game.advanceSeconds(30);game.collectAnimal("ga");
            game.english=true;game.music=0;game.sfx=82;game.save();expected=game.snapshot();
            var duplicates=new ArrayList<>(expected.plots());duplicates.add(expected.plots().get(0));
            GameSave invalid=new GameSave(1,1,0,0,false,false,false,0,0,duplicates,expected.buildings(),expected.animals(),expected.inventory());
            try{repository.save(invalid);throw new AssertionError("Duplicate plot should fail SQL");}catch(IllegalStateException correct){}
            check(repository.load().orElseThrow().coins()==expected.coins(),"SQL transaction restores wallet after a related insert fails");
        }
        try(var repository=new SqliteGameRepository(file,schema)) {
            GameState loaded=new GameState(repository,false);
            check(loaded.coins==expected.coins()&&loaded.english&&loaded.music==0&&loaded.sfx==82,"SQLite cold reload restores wallet/settings");
            check(loaded.pots.size()==16&&loaded.pots.get(0).getLevel()==2&&loaded.buildings.contains(BuildingType.COTTAGE),"SQLite restores plots and buildings");
            check(loaded.animals.get("lon").isFed()&&loaded.inventory.get(ProductType.EGG)==3,"SQLite restores animals and inventory");
            try(Connection sql=DriverManager.getConnection("jdbc:sqlite:"+file);Statement q=sql.createStatement();ResultSet rows=q.executeQuery("PRAGMA integrity_check")){check(rows.next()&&rows.getString(1).equals("ok"),"SQLite integrity");}
        }
    }
    private static void failedSave() {
        class FailingRepository implements GameRepository {
            boolean fail;
            public Optional<GameSave> load(){return Optional.empty();}
            public void save(GameSave save){if(fail)throw new IllegalStateException("Simulated disk error");}
            public void close(){}
        }
        var repository=new FailingRepository();var game=new GameState(repository,false);var service=new GameService(game);GameSave original=game.snapshot();repository.fail=true;
        try{service.act(Map.of("action","harvest","row","0","column","0"));throw new AssertionError("Disk failure should surface");}catch(IllegalStateException correct){}
        check(game.snapshot().equals(original),"Failed save cannot award money or erase a flower in memory");
    }
    private static void http() throws Exception {
        Path file=Files.createTempDirectory("cloudgarden-http-test-").resolve("game.db");
        try(var repository=new SqliteGameRepository(file,Path.of("database/schema.sql"))) {
            var game=new GameState(repository,false);var service=new GameService(game);
            try(var controller=new GameController(service,Path.of("frontend"),0);var client=HttpClient.newHttpClient()) {
                controller.start(false);String base="http://127.0.0.1:"+controller.port();
                var initial=get(client,base+"/api/state");check(initial.statusCode()==200&&initial.body().contains("\"coins\":320"),"Initial state comes from Java");
                Matcher matcher=Pattern.compile("\"token\":\"([^\"]+)\"").matcher(initial.body());check(matcher.find(),"Session token");String token=matcher.group(1);
                check(get(client,base+"/").body().contains("js/app.js")&&get(client,base+"/js/scene.js").statusCode()==200,"HTML and JS served by Java");
                check(post(client,base,token,"action=harvest&row=999&column=0").statusCode()==400,"Reject unknown plots");
                check(post(client,base,token,"action=plant&row=0&column=0&flowerId=daisy").statusCode()==409,"Cannot plant over flowers");
                check(post(client,base,"bad","action=expand").statusCode()==403,"Reject missing session authorization");
                var foreign=HttpRequest.newBuilder(URI.create(base+"/api/action")).header("Content-Type","application/x-www-form-urlencoded").header("X-Game-Token",token).header("Origin","https://example.com").POST(HttpRequest.BodyPublishers.ofString("action=expand")).build();
                check(client.send(foreign,HttpResponse.BodyHandlers.ofString()).statusCode()==403,"Reject a foreign origin");
                var duplicateRequest=request(base,token,"action=harvest&row=0&column=0");
                var one=client.sendAsync(duplicateRequest,HttpResponse.BodyHandlers.ofString());var two=client.sendAsync(duplicateRequest,HttpResponse.BodyHandlers.ofString());
                Set<Integer> statuses=Set.of(one.get().statusCode(),two.get().statusCode());check(statuses.equals(Set.of(200,409))&&game.coins==338&&game.harvestedCount()==1,"Concurrent tabs cannot double harvest");
                check(post(client,base,token,"action=plant&row=0&column=0&flowerId=invalid").statusCode()==400,"Invalid catalog id");
                check(post(client,base,token,"action=plant&row=0&column=0&flowerId=rose").statusCode()==200&&game.coins==326,"Planting charges catalog price");
                check(post(client,base,token,"action=water&row=0&column=0").statusCode()==200&&post(client,base,token,"action=water&row=0&column=0").statusCode()==409,"API watering cooldown");
                service.tick(4);check(post(client,base,token,"action=water&row=0&column=0").statusCode()==200&&game.pots.get(0).getLevel()==2,"API flower evolution");
                check(post(client,base,token,"action=settings&music=-1&sfx=50&language=en").statusCode()==400&&!game.english,"Invalid settings do not mutate state");
                check(post(client,base,token,"action=settings&music=0&sfx=73&language=en").statusCode()==200&&game.english,"Settings API");
                check(post(client,base,token,"action=dig&row=0&column=0").statusCode()==200&&game.harvestedCount()==1,"Dig does not count toward the quest");
                check(post(client,base,token,"action=build&building=COTTAGE").statusCode()==409,"Building prerequisite enforced by Java");
                check(post(client,base,token,"action=feedAnimal&animal=ga").statusCode()==200,"Feed API");service.tick(30);
                check(post(client,base,token,"action=collectAnimal&animal=ga").statusCode()==200&&game.inventory.get(ProductType.EGG)==3,"Egg collection API");
                int coins=game.coins;check(post(client,base,token,"action=sell").statusCode()==200&&game.coins==coins+24,"Sales API");
            }
        }
    }
    private static HttpResponse<String> get(HttpClient client,String url) throws Exception {return client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    private static HttpRequest request(String base,String token,String body){return HttpRequest.newBuilder(URI.create(base+"/api/action")).header("Content-Type","application/x-www-form-urlencoded").header("X-Game-Token",token).POST(HttpRequest.BodyPublishers.ofString(body)).build();}
    private static HttpResponse<String> post(HttpClient client,String base,String token,String body) throws Exception{return client.send(request(base,token,body),HttpResponse.BodyHandlers.ofString());}
}
