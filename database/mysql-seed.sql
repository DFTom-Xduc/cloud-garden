-- Optional demo data. Run ONCE after mysql-schema.sql in a fresh database.
-- If you already have data, do not rerun or replace your player/save with this demo.
USE cloud_garden;
START TRANSACTION;

INSERT INTO flowers VALUES
('daisy','Cúc trắng','Daisy',8,18,10,'#FAEDB2'),
('lavender','Oải hương','Lavender',10,22,12,'#A682D4'),
('rose','Hoa hồng','Rose',12,26,15,'#E77A90'),
('sunflower','Hướng dương','Sunflower',14,30,18,'#F9C144');

INSERT INTO building_types VALUES
('COOP','Chuồng gà','Chicken coop',100),
('MUD_PEN','Bãi bùn cho lợn','Pig mud pen',140),
('COW_BARN','Chuồng bò & bãi cỏ','Cattle barn & paddock',180),
('COTTAGE','Nhà gỗ mới','Timber farmhouse',200);

INSERT INTO product_types VALUES
('EGG','Trứng','Eggs',8),
('PORK','Thịt lợn','Pork',28),
('BEEF','Thịt bò','Beef',40);

INSERT INTO player(id,display_name) VALUES(1,'Người làm vườn');
INSERT INTO buildings VALUES(1,'COOP');
INSERT INTO animals(player_id,species,building_type) VALUES(1,'ga','COOP');
INSERT INTO inventory VALUES(1,'EGG',0),(1,'PORK',0),(1,'BEEF',0);

INSERT INTO plots(player_id,row_index,column_index,flower_id) VALUES
(1,0,0,'daisy'),(1,0,1,'lavender'),(1,0,2,'rose'),(1,0,3,'sunflower'),
(1,1,0,'daisy'),(1,1,1,'lavender'),(1,1,2,'rose'),(1,1,3,'sunflower'),
(1,2,0,'daisy'),(1,2,1,'lavender'),(1,2,2,'rose'),(1,2,3,'sunflower');

COMMIT;

SELECT p.row_index,p.column_index,f.name_vi,p.level
FROM plots p LEFT JOIN flowers f ON f.id=p.flower_id
WHERE p.player_id=1 ORDER BY p.row_index,p.column_index;
