package com.universalna.nsds.controller;

import com.universalna.nsds.model.File;
import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static com.universalna.nsds.controller.PathConstants.ID;
import static com.universalna.nsds.controller.PublicAccessController.ROOT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Validated
@RestController(ROOT)
@Api(value = "Контроллер для работы с опубликованными файлами")
public class PublicAccessController implements StreamingController, Zipper {

    static final String ROOT = "/public";

    private static final String FILES = "/files";

    private static final String META = "/meta";

    private static final String GROUP = "/group";

    @Autowired
    private FileService fileService;

    @GetMapping(value = ROOT + FILES + ID + META)
    @ApiOperation(value = "Метод для получения публичных метаданных файла")
    public ResponseEntity<Metadata> getMetadata(@PathVariable("fileId") @NotBlank final String fileId) {
        return new ResponseEntity<>(fileService.getMetadataWithPreview(fileId), OK);
    }

    @GetMapping(ROOT + FILES + GROUP)
    @ApiOperation(value = "Метод для получения группы публичных ID-метаданных")
    public ResponseEntity<Collection<UUID>> getGroupOfFiles(@RequestParam("key") @NotNull final String key) {
        try {
            final UUID keyId = UUID.fromString(key);
            return new ResponseEntity<>(fileService.getGroupOfFiles(keyId), HttpStatus.OK);
        }catch (IllegalArgumentException ex){
            return new ResponseEntity<>(NOT_FOUND);
        }
    }

    @PutMapping(ROOT + FILES + GROUP + "/zip")
    @ApiOperation(value = "Метод для получения контента группы публичных файлов запакованых в формате zip")
    public ResponseEntity<StreamingResponseBody> streamGroupOfFilesAsZip(@RequestParam("key") @NotNull final String key,
                                                                         @RequestBody         @NotNull final Set<UUID> fileIds) {
        try {
            final UUID keyId = UUID.fromString(key);
            final Collection<File> files = fileService.getContentOfGroupOfFiles(keyId, fileIds);
            return ResponseEntity
                    .status(OK)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.TRANSFER_ENCODING, "binary")
                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"compressed.zip\"")
                    .body(outputStream -> zipFiles(files, outputStream));
        }catch (IllegalArgumentException ex){
            return new ResponseEntity<>(NOT_FOUND);
        }
    }

    @GetMapping(value = ROOT + FILES + ID)
    @ApiOperation(value = "Метод для получения контента публичного файла")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable("fileId") @NotBlank final String fileId) {
        final File content = fileService.get(fileId);
        return streamContent(content);
    }

    @GetMapping(ROOT + FILES)
    @ApiOperation(value = "Метод для получения контента публичного файла")
    public ResponseEntity<StreamingResponseBody> downloadSharedFile(@RequestParam("key") @NotBlank final String key) {
        final File file = fileService.getByShareKey(key);
        return streamContent(file);
    }

    @GetMapping(ROOT + "/shared" + FILES + "/{fileId}")
    @ApiOperation(value = "Метод для получения контента файла c проверкой в таблице shared_file_group")
    public ResponseEntity<StreamingResponseBody> downloadSharedFileWithCheck(@PathVariable("fileId") @NotNull final String fileId,
                                                                                @RequestParam("key") @NotNull final UUID key) {
        File file = fileService.getContentByKeyAndFileId(key, fileId);
        if (file !=null) {
            return streamContent(file);
        }
        return new ResponseEntity<>(NOT_FOUND);
    }

}
