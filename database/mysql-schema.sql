-- Cloud Garden: proposed MySQL 8.0.16+ / 8.4 schema for the OOP/database assignment.
-- Run as a database administrator in Workbench.
-- Creates a NEW MySQL database; does not migrate the existing SQLite save.
CREATE DATABASE IF NOT EXISTS cloud_garden
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cloud_garden;

CREATE TABLE IF NOT EXISTS player (
    id INT NOT NULL AUTO_INCREMENT,
    display_name VARCHAR(80) NOT NULL DEFAULT 'Người làm vườn',
    coins INT NOT NULL DEFAULT 320,
    diamonds INT NOT NULL DEFAULT 5,
    music TINYINT NOT NULL DEFAULT 35,
    sfx TINYINT NOT NULL DEFAULT 65,
    english BOOLEAN NOT NULL DEFAULT FALSE,
    reward_claimed BOOLEAN NOT NULL DEFAULT FALSE,
    expanded BOOLEAN NOT NULL DEFAULT FALSE,
    harvest_total INT NOT NULL DEFAULT 0,
    game_minutes DOUBLE NOT NULL DEFAULT 480,
    PRIMARY KEY (id),
    CHECK (coins >= 0 AND diamonds >= 0),
    CHECK (music BETWEEN 0 AND 100 AND sfx BETWEEN 0 AND 100),
    CHECK (english IN (0,1) AND reward_claimed IN (0,1) AND expanded IN (0,1)),
    CHECK (harvest_total >= 0 AND game_minutes >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS flowers (
    id VARCHAR(40) NOT NULL,
    name_vi VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    seed_price INT NOT NULL,
    sale_price INT NOT NULL,
    base_power INT NOT NULL,
    color CHAR(7) NOT NULL,
    PRIMARY KEY (id),
    CHECK (seed_price >= 0 AND sale_price > 0 AND base_power > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS plots (
    player_id INT NOT NULL,
    row_index INT NOT NULL,
    column_index INT NOT NULL,
    flower_id VARCHAR(40) NULL,
    level TINYINT NOT NULL DEFAULT 1,
    water TINYINT NOT NULL DEFAULT 0,
    cooldown DOUBLE NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id, row_index, column_index),
    CONSTRAINT fk_plot_player FOREIGN KEY (player_id) REFERENCES player(id),
    CONSTRAINT fk_plot_flower FOREIGN KEY (flower_id) REFERENCES flowers(id),
    CHECK (row_index >= 0 AND column_index >= 0),
    CHECK (level BETWEEN 1 AND 3 AND water BETWEEN 0 AND 1 AND cooldown >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS building_types (
    code VARCHAR(20) NOT NULL,
    name_vi VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    price INT NOT NULL,
    PRIMARY KEY (code),
    CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS buildings (
    player_id INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    PRIMARY KEY (player_id, type),
    CONSTRAINT fk_building_player FOREIGN KEY (player_id) REFERENCES player(id),
    CONSTRAINT fk_building_type FOREIGN KEY (type) REFERENCES building_types(code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS animals (
    player_id INT NOT NULL,
    species ENUM('ga','lon','bo','cuu','de') NOT NULL,
    building_type VARCHAR(20) NOT NULL,
    fed BOOLEAN NOT NULL DEFAULT FALSE,
    progress DOUBLE NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id, species),
    CONSTRAINT fk_animal_habitat FOREIGN KEY (player_id, building_type)
        REFERENCES buildings(player_id, type),
    CHECK (fed IN (0,1) AND progress >= 0),
    CONSTRAINT ck_species_habitat CHECK (
        (species = 'ga' AND building_type = 'COOP') OR
        (species = 'lon' AND building_type = 'MUD_PEN') OR
        (species = 'bo' AND building_type = 'COW_BARN') OR
        (species = 'cuu' AND building_type = 'SHEEP_BARN') OR
        (species = 'de' AND building_type = 'GOAT_BARN')
    )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_types (
    code VARCHAR(20) NOT NULL,
    name_vi VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    price INT NOT NULL,
    PRIMARY KEY (code),
    CHECK (price > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory (
    player_id INT NOT NULL,
    product VARCHAR(20) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id, product),
    CONSTRAINT fk_inventory_player FOREIGN KEY (player_id) REFERENCES player(id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product) REFERENCES product_types(code),
    CHECK (quantity >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS farm_settings (
    player_id INT NOT NULL,
    guard_dog BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (player_id),
    CONSTRAINT fk_farm_settings_player FOREIGN KEY (player_id) REFERENCES player(id),
    CHECK (guard_dog IN (0,1))
) ENGINE=InnoDB;

SHOW TABLES;
