PRAGMA foreign_keys = ON;
CREATE TABLE IF NOT EXISTS player (
    id INTEGER PRIMARY KEY CHECK(id = 1),
    coins INTEGER NOT NULL CHECK(coins >= 0),
    diamonds INTEGER NOT NULL CHECK(diamonds >= 0),
    music INTEGER NOT NULL CHECK(music BETWEEN 0 AND 100),
    sfx INTEGER NOT NULL CHECK(sfx BETWEEN 0 AND 100),
    english INTEGER NOT NULL CHECK(english IN (0,1)),
    reward_claimed INTEGER NOT NULL CHECK(reward_claimed IN (0,1)),
    expanded INTEGER NOT NULL CHECK(expanded IN (0,1)),
    harvest_total INTEGER NOT NULL CHECK(harvest_total >= 0),
    game_minutes REAL NOT NULL CHECK(game_minutes >= 0)
);
CREATE TABLE IF NOT EXISTS plots (
    player_id INTEGER NOT NULL REFERENCES player(id),
    row_index INTEGER NOT NULL,
    column_index INTEGER NOT NULL,
    flower_id TEXT,
    level INTEGER NOT NULL CHECK(level BETWEEN 1 AND 3),
    water INTEGER NOT NULL CHECK(water BETWEEN 0 AND 1),
    cooldown REAL NOT NULL CHECK(cooldown >= 0),
    PRIMARY KEY(player_id,row_index,column_index)
);
CREATE TABLE IF NOT EXISTS buildings (
    player_id INTEGER NOT NULL REFERENCES player(id),
    type TEXT NOT NULL,
    PRIMARY KEY(player_id,type)
);
CREATE TABLE IF NOT EXISTS animals (
    player_id INTEGER NOT NULL REFERENCES player(id),
    species TEXT NOT NULL CHECK(species IN ('ga','lon','bo')),
    fed INTEGER NOT NULL CHECK(fed IN (0,1)),
    progress REAL NOT NULL CHECK(progress >= 0),
    PRIMARY KEY(player_id,species)
);
CREATE TABLE IF NOT EXISTS inventory (
    player_id INTEGER NOT NULL REFERENCES player(id),
    product TEXT NOT NULL CHECK(product IN ('EGG','PORK','BEEF')),
    quantity INTEGER NOT NULL CHECK(quantity >= 0),
    PRIMARY KEY(player_id,product)
);
PRAGMA user_version = 1;
