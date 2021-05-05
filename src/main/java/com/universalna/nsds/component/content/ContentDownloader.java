package com.universalna.nsds.component.content;

import java.io.InputStream;

public interface ContentDownloader {

    InputStream getContentByUrl(String url);
}
