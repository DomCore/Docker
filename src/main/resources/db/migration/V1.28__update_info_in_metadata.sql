UPDATE metadata SET info = jsonb_set(info, '{rotate}', '0') where info->>'rotate' not in('0','90','180','270');