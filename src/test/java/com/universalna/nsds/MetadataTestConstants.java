package com.universalna.nsds;

import java.util.UUID;

public final class MetadataTestConstants {

    public static final String BLANK_STRING = "   ";
    public static final String FILE_ID_STRING = "e8a174a4-20a3-4d43-9eec-84d4140695ad";
    public static final UUID FILE_ID = UUID.fromString(FILE_ID_STRING);
    public static final String FILE_NAME = "image.jpg";
    public static final String RELATION_ID = "mockedRelationId";
    public static final String DESCRIPTION = "mockDescription";
    public static final Long FILE_SIZE = 13482898L;
    private MetadataTestConstants() {
    }
}
