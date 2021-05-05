package com.universalna.nsds.controller;

import com.universalna.nsds.controller.dto.FileTagDTO;
import com.universalna.nsds.model.FileTag;
import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.model.ModelWithLastModified;
import com.universalna.nsds.model.Origin;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.model.Status;
import com.universalna.nsds.service.postgres.TransactionalReadOnlyMetadataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static com.universalna.nsds.Profiles.METADATA;
import static com.universalna.nsds.controller.PathConstants.ID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.OK;

@Profile(METADATA)
@Validated
@RestController
@Api(value = "Контроллер для работы с метаданными файлов")
public class MetadataController extends AbstractFileController {

    private static final String META = "/meta";

    @Autowired
    private Mapper mapper;

    @Autowired
    private TransactionalReadOnlyMetadataService transactionalReadOnlyMetadataService;

    @PutMapping(value = ROOT + ID)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Метод для изменения статуса файла")
    public ResponseEntity<ModelWithLastModified<String>> changeFileStatus(@PathVariable("fileId") @NotBlank final String fileId,
                                                                  @RequestParam("status")           final Status status){
        return new ResponseEntity<>(fileService.changeFileStatus(fileId, status), OK);
    }

    @GetMapping(value = ROOT + "/{relation}" + "/{relationId}")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Метод для получения метаданных файлов по связи")
    public ResponseEntity<Collection<Metadata>> getRelatedMetadata(@PathVariable("relation") final Relation relation,
                                                                   @PathVariable("relationId") @NotBlank final String relationId,
                                                                   @RequestParam(value = "origin", required = false) Set<Origin> origins) {
        final Collection<Metadata> metadata = fileService.getRelatedMetadata(relation, relationId);
        if (origins == null || origins.isEmpty()) {
            return new ResponseEntity<>(metadata, OK);
        }
        return new ResponseEntity<>(metadata.stream().filter(m -> origins.contains(m.getOrigin())).collect(toList()), OK);
    }

    @GetMapping(value = ROOT + "/{relation}" + "/{relationId}"+ "/inactive")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Метод для получения неактивных метаданных файлов")
    public ResponseEntity<Collection<Metadata>> getInactiveRelatedMetadata(@PathVariable("relation")             final Relation relation,
                                                                           @PathVariable("relationId") @NotBlank final String relationId) {
        return new ResponseEntity<>(fileService.getInactiveFilesMetadata(relation, relationId), OK);
    }

    @GetMapping(ROOT + ID + "/publish")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"api_nsds_getPublicUrl\")")
    @ApiOperation(value = "Метод для получения публичной ссылки на файл")
    public ResponseEntity<String> createSharedUri(@PathVariable("fileId") @NotBlank final String fileId) {
        return new ResponseEntity<>(fileService.createSharedUri(fileId), HttpStatus.OK);
    }

    @PostMapping(ROOT + "/group/publish")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\")")
    @ApiOperation(value = "Метод для получения публичной ссылки на группу файлов")
    public ResponseEntity<String> createSharedUriToGroupOfFiles(@RequestBody @NotEmpty final Collection<UUID> fileIds) {
        return new ResponseEntity<>(fileService.createSharedUriToGroupOfFiles(fileIds), HttpStatus.OK);
    }

    @GetMapping(value = ROOT + ID + META)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Метод для получения метаданных файла")
    public ResponseEntity<Metadata> getMetadata(@PathVariable("fileId") @NotBlank final String fileId) {
        final Metadata metadata = fileService.getMetadata(fileId);
        return new ResponseEntity<>(metadata, OK);
    }

    @Deprecated
    @GetMapping(value = ROOT + ID + META + "/testReadOnly")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Метод для получения метаданных файла")
    public ResponseEntity<Metadata> getMetadataTest(@PathVariable("fileId") @NotBlank final String fileId) {
        final Metadata metadata = transactionalReadOnlyMetadataService.getMetadata(fileId);
        return new ResponseEntity<>(metadata, OK);
    }

    @DeleteMapping(value = ROOT + ID)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"api_nsds_delete\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Метод для удаления файла")
    public ResponseEntity<ModelWithLastModified<String>> delete(@PathVariable("fileId") @NotBlank final String fileId) {
        return new ResponseEntity<>(fileService.delete(fileId), OK);
    }

    @PutMapping(value = ROOT + ID + META + "/name", consumes = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\")")
    @ApiOperation(value = "Метод для обновления имени файла без учета формата")
    public ResponseEntity<ModelWithLastModified<String>> updateName(@PathVariable("fileId") @NotBlank final String fileId,
                                                                    @RequestBody            @NotBlank final String name) {
        return new ResponseEntity<>(fileService.updateName(fileId, name), OK);
    }

    @PutMapping(value = ROOT + ID + META + "/description", consumes = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\")")
    @ApiOperation(value = "Метод для обновления описания файла")
    public ResponseEntity<ModelWithLastModified<String>> updateDescription(@PathVariable("fileId") @NotBlank final String fileId,
                                                                           @RequestBody(required = false)    final String description) {
        return new ResponseEntity<>(fileService.updateDescription(fileId, description), OK);
    }

    @PutMapping(value = ROOT + ID + META + "/tags")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\")")
    @ApiOperation(value = "Метод для обновления тегов файла")
    public ResponseEntity<ModelWithLastModified<Collection<FileTag>>> updateTags(@PathVariable("fileId") @NotBlank final String fileId,
                                                                                 @RequestBody final Set<FileTagDTO> tags) {
        final Set<com.universalna.nsds.model.FileTag> modelTags = tags.stream().map(mapper::toModel).collect(toSet());
        return ResponseEntity.ok(fileService.updateTags(fileId, modelTags));
    }

    @PutMapping(value = ROOT + ID + META + "/info")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\")")
    @ApiOperation(value = "Метод для обновления дополнительной информации по файлу")
    public ResponseEntity<ModelWithLastModified<String>> updateInfo(@PathVariable("fileId") @NotBlank final String fileId,
                                                                    @RequestBody                      final JSONObject infoJson) {
        return ResponseEntity.ok(fileService.updateInfo(fileId, infoJson.toJSONString()));
    }

    @PostMapping(value = ROOT + "/{id}/copy/{copyToRelation}/{copyToRelationId}")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\")")
    @ApiOperation(value = "Метод для копирования файла к другой сущности")
    public ResponseEntity<UUID> copyMetadataToRelation(@PathVariable("id")                         final UUID id,
                                                       @PathVariable("copyToRelation")             final Relation copyToRelation,
                                                       @PathVariable("copyToRelationId") @NotBlank final String copyToRelationId,
                                                       @RequestBody                                final Set<FileTagDTO> replacementTags) {
        return ResponseEntity.ok(fileService.copyMetadataAsNew(id, copyToRelation, copyToRelationId, replacementTags));
    }

    @GetMapping(value = ROOT + "/{id}/edit")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\")")
    @ApiOperation(value = "Метод для создания ссылки для редактирования файла, для файлов котоыре это поддерживают")
    public ResponseEntity<String> createUrlToEditableFile(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok(fileService.createUrlToEditableFile(id));
    }

    @GetMapping(value = ROOT + "/tags")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\")")
    @ApiOperation(value = "получить список уникальных тегов ко всем сущностям")
    public ResponseEntity<List<String>> getAllTagsFromAllEntities() {
        return ResponseEntity.ok(fileService.getAllTagsFromAllEntities().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(toList()));
    }

    @GetMapping(value = ROOT + "/tags/grouped")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\")")
    @ApiOperation(value = "получить список уникальных тегов ко всем сущностям сгруппированный по сущностям")
    public ResponseEntity<Map<String, Set<String>>> getAllTagsFromAllEntitiesGroupedWithRelations() {
        final Map<String, Set<String>> allTagsFromAllEntitiesGroupedWithRelations = fileService.getAllTagsFromAllEntitiesGroupedWithRelations();
        final SortedMap<String, Set<String>> sortedAlphabeticallyByTags = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sortedAlphabeticallyByTags.putAll(allTagsFromAllEntitiesGroupedWithRelations);
        return ResponseEntity.ok(sortedAlphabeticallyByTags);
    }

    @GetMapping(value = ROOT + "/{relation}" + "/{relationId}"+ "/tags")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\")")
    @ApiOperation(value = "получить список уникальных тегов по сущности")
    public ResponseEntity<List<String>> getTagsByEntity(@PathVariable("relation")             final Relation relation,
                                                        @PathVariable("relationId") @NotBlank final String relationId) {
        return ResponseEntity.ok(fileService.getAllTagsByRelation(relation, relationId).stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(toList()));
    }

    @PostMapping(value = ROOT + "/{relation}" + "/{relationId}"+ "/archive")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Поменять статус файла на архивный")
    public ResponseEntity<Collection<ModelWithLastModified<String>>> archive(@PathVariable("relation") final Relation relation,
                                                         @PathVariable("relationId") @NotBlank final String relationId) {
        return new ResponseEntity<>(fileService.archive(relation, relationId), OK);
    }

    @PostMapping(value = ROOT + "/{relation}" + "/{relationId}"+ "/archive/revert")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Поменять статус файла с архивного на активный")
    public ResponseEntity<Collection<ModelWithLastModified<String>>> revertFromArchive(@PathVariable("relation") final Relation relation,
                                                                   @PathVariable("relationId") @NotBlank final String relationId) {
        return new ResponseEntity<>(fileService.revertFromArchive(relation, relationId), OK);
    }

    // method developed for profitsoft, do not touch.
    @PostMapping(ROOT + "/temp/persist" + "/{relation}" + "/{relationId}")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\")")
    @ApiOperation(value = "Поменять статус файла с временного на постоянный")
    public ResponseEntity<Void> setTempFilesToPersistent(@PathVariable("relation")             final Relation relation,
                                                         @PathVariable("relationId") @NotBlank final String relationId,
                                                         @RequestBody                          final Set<UUID> fileIds) {
        fileService.setTempFilesToPersistent(relation, relationId, fileIds);
        return new ResponseEntity<>(OK);
    }

}
