package com.universalna.nsds.controller;

import com.universalna.nsds.model.MetadataAudit;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.service.MetadataAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.envers.RevisionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.universalna.nsds.controller.FileAuditController.ROOT;
import static com.universalna.nsds.controller.PathConstants.FILES;
import static com.universalna.nsds.controller.PathConstants.ID;

@Validated
@RestController(value = ROOT)
@Api(value = "Контроллер для работы с таблицей аудита метаданных файлов")
public class FileAuditController {

    private static final String AUDIT = "/audit";
    static final String ROOT = AUDIT + FILES;

    @Autowired
    private MetadataAuditService auditService;

    @GetMapping(value = ROOT + ID)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\")")
    @ApiOperation(value = "Получить версии метаданных")
    public ResponseEntity<List<MetadataAudit>> getMetadataVersions(@PathVariable("fileId") @Valid @NotNull final UUID id) {
        return new ResponseEntity<>(auditService.getMetadataVersionsSorted(id), HttpStatus.OK);
    }

    @GetMapping(value = ROOT + "/{relation}" + "/{relationId}")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\")")
    @ApiOperation(value = "Получить версии метаданных по связи")
    public ResponseEntity<List<MetadataAudit>> getMetadataVersionsByRelation(@PathVariable("relation") final Relation relation,
                                                                   @PathVariable("relationId") @NotBlank final String relationId,
                                                                   @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                                   @RequestParam(value = "to", required = false)   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to,
                                                                   @RequestParam(value = "action", required = false) final RevisionType action) {
        final List<MetadataAudit> metadataVersionsByRelation = auditService.getMetadataVersionsByRelation(relation, relationId, from, to, action);
        return ResponseEntity.ok(metadataVersionsByRelation);
    }

    @PostMapping(value = ROOT + ID + "/{revision}")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\")")
    @ApiOperation(value = "Откатить метаданные на версию")
    public ResponseEntity<Void> revertToMetadataRevision(@PathVariable("fileId")   @Valid @NotNull final UUID id,
                                                         @PathVariable("revision") @Valid @NotNull final Long revisionNumber) {
        auditService.revert(id, revisionNumber);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
