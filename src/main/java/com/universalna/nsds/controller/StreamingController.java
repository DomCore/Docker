package com.universalna.nsds.controller;

import com.universalna.nsds.model.File;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

import static org.springframework.http.HttpStatus.OK;

public interface StreamingController {

    default ResponseEntity<StreamingResponseBody> streamContent(final InputStream stream) {
        return ResponseEntity
                .status(OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.TRANSFER_ENCODING, "binary")
                .body(toStreamingResponseBody(stream));
    }

    default ResponseEntity<StreamingResponseBody> streamContent(final InputStream stream, final String filename) {
        return ResponseEntity
                .status(OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.TRANSFER_ENCODING, "binary")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename +"\"")
                .body(toStreamingResponseBody(stream));
    }

    default ResponseEntity<StreamingResponseBody> streamContent(final File file) {
        return ResponseEntity
                .status(OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.TRANSFER_ENCODING,"binary")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getOriginalName() +"\"")
                .body(toStreamingResponseBody(file.getContent()));
    }

    private StreamingResponseBody toStreamingResponseBody(final InputStream inputStream) {
        return inputStream::transferTo;
    }
}
