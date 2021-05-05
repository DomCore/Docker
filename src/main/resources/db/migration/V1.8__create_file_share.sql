CREATE TABLE SHARED_FILE_GROUP (ID           UUID PRIMARY KEY,
                                METADATA_IDS JSONB NOT NULL,
                                KEY          UUID UNIQUE NOT NULL,
                                INFO         JSONB)
