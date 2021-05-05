CREATE TABLE SHARED_FILE (ID          UUID PRIMARY KEY,
                          METADATA_ID UUID NOT NULL REFERENCES metadata(id) ON DELETE CASCADE,
                          KEY         UUID UNIQUE NOT NULL,
                          INFO        JSONB)
