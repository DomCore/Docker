package com.universalna.nsds.component.content;

import com.universalna.nsds.exception.IoExceptionHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class OkHttpContentDownloader implements ContentDownloader, IoExceptionHandler {

    @Autowired
    private OkHttpClient okHttpClient;

    public InputStream getContentByUrl(final String url) {
        final Request request = new Request.Builder().url(url)
                .build();
        final Response response = tryIoOperation(() -> okHttpClient.newCall(request).execute());
        return  response.body().byteStream();
    }
}
