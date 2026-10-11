package tests;

import Entity.*;
import Service.*;
import Repository.*;
import java.nio.file.*;
import java.util.*;

public final class QuestTest {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        Path db=Files.createTempDirectory("garden-quests-").resolve("game.db");
        GameSave expected;
        try(var repository=new SqliteGameRepository(db,Path.of("database/schema.sql"))){
            GameState game=new GameState(repository,false);GameService service=new GameService(game);
            check(QuestCatalog.ALL.size()==20,"Twenty quests available");
            check(!game.claimQuest("eggs"),"Cannot claim an incomplete quest");
            int wallet=game.coins;
            for(int cycle=0;cycle<2;cycle++){
                check(game.feedAnimal("ga"),"Feed chicken");
                check(!game.feedAnimal("ga"),"Duplicate feed rejected");
                game.advanceSeconds(30);check(game.collectAnimal("ga"),"Collect eggs");
            }
            check(game.questCount(QuestCatalog.find("eggs"))==6,"Collected product quantities tracked");
            check(game.questCount(QuestCatalog.find("feed_ga"))==2,"Rejected feed does not increase progress");
            int before=game.coins;check(game.claimQuest("eggs")&&!game.claimQuest("eggs")&&game.coins==before+65,"Reward granted exactly once");
            check(game.sellProducts()==48&&game.questCount(QuestCatalog.find("market"))==48,"Sale progress uses earned money");
            game.sellProducts();check(game.questCount(QuestCatalog.find("market"))==48,"Empty sale does not count");
            PlantPot p=game.pots.get(0);game.water(p);game.advanceSeconds(4);game.water(p);game.advanceSeconds(4);game.water(p);game.advanceSeconds(4);game.water(p);
            check(game.questCount(QuestCatalog.find("bloom"))==1,"Level 3 counted at transition");
            check(!game.water(p)&&game.questCount(QuestCatalog.find("water"))==4,"Failed watering not counted");
            check(game.dig(p)&&game.questCount(QuestCatalog.find("first_harvest"))==0,"Digging does not count");
            game.plant(p,FlowerCatalog.ALL.get(0));check(game.questCount(QuestCatalog.find("sow"))==1,"Plant counted");
            expected=game.snapshot();
            check(service.state().containsKey("quests"),"API exposes quests");
            try{service.act(Map.of("action","claimQuest","quest","eggs"));throw new AssertionError("Already claimed should conflict");}catch(GameService.Conflict correct){}
            check(game.snapshot().equals(expected),"Rejected claim rolls back all state");
        }
        try(var repository=new SqliteGameRepository(db,Path.of("database/schema.sql"))){
            GameState loaded=new GameState(repository,false);
            check(loaded.snapshot().equals(expected),"Quest counters and claimed rewards survive cold reload");
            check(!loaded.claimQuest("eggs"),"Cannot claim again after restart");
        }
        // Old snapshot constructors migrate without resetting any wallet or plots.
        GameState old=new GameState(false);old.coins=777;old.rewardClaimed=true;GameSave prior=old.snapshot();
        GameSave legacy=new GameSave(prior.coins(),prior.diamonds(),prior.music(),prior.sfx(),prior.english(),prior.rewardClaimed(),prior.expanded(),prior.harvestTotal(),prior.gameMinutes(),prior.plots(),prior.buildings(),prior.animals(),prior.inventory());
        old.restore(legacy);check(old.coins==777&&!old.claimReward(),"Legacy first reward remains claimed");
        System.out.println("PASS: quest rules, no duplicate rewards, cold reload and legacy compatibility");
    }
}
