-- Align gps_points.id with the project-wide UUID identifier convention.

ALTER TABLE gps_points ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE gps_points DROP CONSTRAINT gps_points_pkey;
ALTER TABLE gps_points ADD COLUMN id_uuid UUID;
UPDATE gps_points SET id_uuid = gen_random_uuid();
ALTER TABLE gps_points DROP COLUMN id;
ALTER TABLE gps_points RENAME COLUMN id_uuid TO id;
ALTER TABLE gps_points ALTER COLUMN id SET NOT NULL;
ALTER TABLE gps_points ADD PRIMARY KEY (id);
