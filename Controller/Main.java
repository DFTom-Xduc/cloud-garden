package Controller;

import Repository.SqliteGameRepository;
import Service.GameService;
import Service.GameState;
import java.nio.file.*;

public final class Main {
    public static void main(String[] args) throws Exception {
        Path root=Path.of(System.getProperty("cloudgarden.root", ".")).toAbsolutePath().normalize();
        int port=args.length==2&&args[0].equals("--port")?Integer.parseInt(args[1]):8080;
        if(!Files.isRegularFile(root.resolve("frontend/index.html")))
            throw new IllegalStateException("Run run.ps1 from the project folder.");
        Path database=Path.of(System.getProperty("cloudgarden.db",root.resolve("database/cloudgarden.db").toString()));
        var repository=new SqliteGameRepository(database,root.resolve("database/schema.sql"));
        var game=new GameState(repository,Boolean.parseBoolean(System.getProperty("cloudgarden.migrateLegacy","true")));
        var controller=new GameController(new GameService(game),root.resolve("frontend"),port);
        Runtime.getRuntime().addShutdownHook(new Thread(()->{try{controller.close();}finally{repository.close();}},"save-garden"));
        controller.start(true);
        System.out.println("Cloud Garden: http://localhost:"+controller.port());
        System.out.println("SQLite: "+database.toAbsolutePath());
        System.out.println("Keep this terminal running. Press Ctrl+C to save and stop.");
    }
}
