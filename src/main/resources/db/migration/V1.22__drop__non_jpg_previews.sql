UPDATE metadata SET preview = null WHERE name NOT ILIKE '%.jpg' AND
                                         name NOT ILIKE '%.pdf' AND
                                         name NOT ILIKE '%.tiff' AND
                                         name NOT ILIKE '%.tif';