package Repository;

import Entity.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** JDBC repository. Every save replaces the related rows in one atomic SQL transaction. */
public final class SqliteGameRepository implements GameRepository {
    private final Connection connection;
    public SqliteGameRepository(Path file,Path schema) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Class.forName("org.sqlite.JDBC");
            connection=DriverManager.getConnection("jdbc:sqlite:"+file.toAbsolutePath());
            try(Statement statement=connection.createStatement()) {
                statement.execute("PRAGMA busy_timeout=5000");
                statement.execute("PRAGMA journal_mode=WAL");
                for(String sql:Files.readString(schema).split(";"))if(!sql.isBlank())statement.execute(sql);
            }
        } catch(Exception error){throw new IllegalStateException("Cannot open SQLite database: "+file,error);}
    }
    @Override public synchronized Optional<GameSave> load() {
        try(Statement query=connection.createStatement();ResultSet player=query.executeQuery("SELECT * FROM player WHERE id=1")) {
            if(!player.next())return Optional.empty();
            int coins=player.getInt("coins"),diamonds=player.getInt("diamonds"),music=player.getInt("music"),sfx=player.getInt("sfx"),harvests=player.getInt("harvest_total");
            boolean english=player.getBoolean("english"),claimed=player.getBoolean("reward_claimed"),expanded=player.getBoolean("expanded");
            double minutes=player.getDouble("game_minutes");
            List<GameSave.Plot> plots=new ArrayList<>();Set<BuildingType> buildings=EnumSet.noneOf(BuildingType.class);
            List<GameSave.Livestock> animals=new ArrayList<>();Map<ProductType,Integer> stock=new EnumMap<>(ProductType.class);
            try(Statement s=connection.createStatement();ResultSet rows=s.executeQuery("SELECT * FROM plots WHERE player_id=1 ORDER BY column_index>=4,row_index,column_index")) {
                while(rows.next())plots.add(new GameSave.Plot(rows.getInt("row_index"),rows.getInt("column_index"),rows.getString("flower_id"),rows.getInt("level"),rows.getInt("water"),rows.getDouble("cooldown")));
            }
            try(Statement s=connection.createStatement();ResultSet rows=s.executeQuery("SELECT type FROM buildings WHERE player_id=1")) {while(rows.next())buildings.add(BuildingType.valueOf(rows.getString(1)));}
            try(Statement s=connection.createStatement();ResultSet rows=s.executeQuery("SELECT * FROM animals WHERE player_id=1")) {while(rows.next())animals.add(new GameSave.Livestock(rows.getString("species"),rows.getBoolean("fed"),rows.getDouble("progress")));}
            try(Statement s=connection.createStatement();ResultSet rows=s.executeQuery("SELECT * FROM inventory WHERE player_id=1")) {while(rows.next())stock.put(ProductType.valueOf(rows.getString("product")),rows.getInt("quantity"));}
            return Optional.of(new GameSave(coins,diamonds,music,sfx,english,claimed,expanded,harvests,minutes,plots,buildings,animals,stock));
        } catch(SQLException error){throw new IllegalStateException("Cannot load game",error);}
    }
    @Override public synchronized void save(GameSave save) {
        try {
            connection.setAutoCommit(false);
            try(PreparedStatement p=connection.prepareStatement("INSERT INTO player VALUES(1,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET coins=excluded.coins,diamonds=excluded.diamonds,music=excluded.music,sfx=excluded.sfx,english=excluded.english,reward_claimed=excluded.reward_claimed,expanded=excluded.expanded,harvest_total=excluded.harvest_total,game_minutes=excluded.game_minutes")) {
                p.setInt(1,save.coins());p.setInt(2,save.diamonds());p.setInt(3,save.music());p.setInt(4,save.sfx());p.setBoolean(5,save.english());
                p.setBoolean(6,save.rewardClaimed());p.setBoolean(7,save.expanded());p.setInt(8,save.harvestTotal());p.setDouble(9,save.gameMinutes());
                p.executeUpdate();
            }
            try(Statement s=connection.createStatement()){for(String table:List.of("plots","buildings","animals","inventory"))s.executeUpdate("DELETE FROM "+table+" WHERE player_id=1");}
            try(PreparedStatement p=connection.prepareStatement("INSERT INTO plots VALUES(1,?,?,?,?,?,?)")) {
                for(var plot:save.plots()){p.setInt(1,plot.row());p.setInt(2,plot.column());p.setString(3,plot.flowerId());p.setInt(4,plot.level());p.setInt(5,plot.water());p.setDouble(6,plot.cooldown());p.addBatch();}p.executeBatch();
            }
            try(PreparedStatement p=connection.prepareStatement("INSERT INTO buildings VALUES(1,?)")){for(var building:save.buildings()){p.setString(1,building.name());p.addBatch();}p.executeBatch();}
            try(PreparedStatement p=connection.prepareStatement("INSERT INTO animals VALUES(1,?,?,?)")){for(var animal:save.animals()){p.setString(1,animal.id());p.setBoolean(2,animal.fed());p.setDouble(3,animal.progress());p.addBatch();}p.executeBatch();}
            try(PreparedStatement p=connection.prepareStatement("INSERT INTO inventory VALUES(1,?,?)")){for(var item:save.inventory().entrySet()){p.setString(1,item.getKey().name());p.setInt(2,item.getValue());p.addBatch();}p.executeBatch();}
            connection.commit();
        } catch(SQLException error) {
            try{connection.rollback();}catch(SQLException rollback){error.addSuppressed(rollback);}
            throw new IllegalStateException("Cannot save game transaction",error);
        } finally {try{connection.setAutoCommit(true);}catch(SQLException ignored){}}
    }
    @Override public synchronized void close() {try{connection.close();}catch(SQLException error){throw new IllegalStateException(error);}}
}
