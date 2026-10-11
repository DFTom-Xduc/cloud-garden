-- Optional reference migration for an EXISTING MySQL cloud_garden database.
-- Run once instead of rerunning mysql-seed.sql. Java still uses SQLite.
-- MySQL DDL commits independently; back up the database before applying manually.
USE cloud_garden;
ALTER TABLE animals DROP CHECK ck_species_habitat;
ALTER TABLE animals MODIFY species ENUM('ga','lon','bo','cuu','de') NOT NULL,
ADD CONSTRAINT ck_species_habitat CHECK (
    (species='ga' AND building_type='COOP') OR
    (species='lon' AND building_type='MUD_PEN') OR
    (species='bo' AND building_type='COW_BARN') OR
    (species='cuu' AND building_type='SHEEP_BARN') OR
    (species='de' AND building_type='GOAT_BARN')
);
INSERT IGNORE INTO building_types VALUES
('SHEEP_BARN','Chuồng cừu & đồng cỏ','Sheep barn & pasture',240),
('GOAT_BARN','Trang trại dê','Goat farm',230);
INSERT IGNORE INTO product_types VALUES
('WOOL','Lông cừu','Wool',22),('GOAT_MEAT','Thịt dê','Goat meat',38);
CREATE TABLE IF NOT EXISTS farm_settings (
    player_id INT NOT NULL,
    guard_dog BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (player_id),
    CONSTRAINT fk_farm_settings_player FOREIGN KEY (player_id) REFERENCES player(id),
    CHECK (guard_dog IN (0,1))
) ENGINE=InnoDB;
