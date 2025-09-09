CREATE TABLE IF NOT EXISTS version
(
    version INTEGER DEFAULT -1,
    singleton_value DEFAULT 0,
    PRIMARY KEY (singleton_value)
);

CREATE TABLE IF NOT EXISTS structure
(
    uuid           BINARY(16),
    world_uuid     BINARY(16),
    origin_x       INTEGER,
    origin_y       INTEGER,
    origin_z       INTEGER,
    transformation INTEGER,
    schematic      TEXT,
    structure_type TEXT,
    creation_date  INTEGER,
    PRIMARY KEY (uuid)
);

CREATE INDEX IF NOT EXISTS main.structure_world ON structure (world_uuid, structure_type);

CREATE TABLE IF NOT EXISTS inventory
(
    structure_uuid BINARY(16),
    inventory_type TEXT,
    inventory_size INTEGER,
    FOREIGN KEY (structure_uuid)
        REFERENCES structure (uuid)
        ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (structure_uuid, inventory_type)
);

CREATE TABLE IF NOT EXISTS inventory_content
(
    structure_uuid BINARY(16),
    pos            INTEGER,
    item_content   JSON,
    inventory_type TEXT,
    FOREIGN KEY (structure_uuid, inventory_type)
        REFERENCES inventory (structure_uuid, inventory_type)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS structure_state
(
    structure_uuid BINARY(16),
    state          TEXT,
    time_stamp     INTEGER,
    change         TEXT,
    FOREIGN KEY (structure_uuid)
        REFERENCES structure (uuid)
        ON UPDATE CASCADE ON DELETE CASCADE
);