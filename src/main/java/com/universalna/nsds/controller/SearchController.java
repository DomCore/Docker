package com.universalna.nsds.controller;

import com.universalna.nsds.exception.NotFoundException;
import com.universalna.nsds.service.search.profitsoft.ClaimInfoDto;
import com.universalna.nsds.service.search.profitsoft.ClaimInfoQueryDto;
import com.universalna.nsds.service.search.profitsoft.ContractInfoDto;
import com.universalna.nsds.service.search.profitsoft.ContractInfoQueryDto;
import com.universalna.nsds.service.search.profitsoft.Page;
import com.universalna.nsds.service.search.profitsoft.ProfitsoftSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

//TODO: проверить есть ли у чатботов потребность в этом контроллере
@Deprecated
@RestController
@Validated
public class SearchController {

    private static final String ROOT = "/search";

    @Autowired
    private ProfitsoftSearchService searchService;

    @PutMapping(ROOT)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"chatbot\")")
    public ResponseEntity<Page<ClaimInfoDto>> searchInsuranceCases(@RequestBody final ClaimInfoQueryDto searchParamsDto) {
        return ResponseEntity.ok(searchService.listClaimInfos(searchParamsDto));
    }

    //    пошук по полям: номер сд, номер су, піб потерпілого, піб страхувальника, номер договору, обєкт, реєстраційний номер ТЗ (застрахованого та потерпілого), vin код
    @GetMapping(ROOT)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"chatbot\")")
    public ResponseEntity<List<ClaimInfoDto>> quickSearchInsuranceCase(@RequestParam(name = "value") @NotBlank final String value) {
        final ClaimInfoQueryDto insuranceCaseNumber = new ClaimInfoQueryDto();
        insuranceCaseNumber.setSettlementCaseNumber(value);
        final ClaimInfoQueryDto insuranceCaseNotificationNumber = new ClaimInfoQueryDto();
        insuranceCaseNotificationNumber.setNoticeNumber(value);
        final ClaimInfoQueryDto victimName = new ClaimInfoQueryDto();
        victimName.setInjuredName(value);
        final ClaimInfoQueryDto insurantName = new ClaimInfoQueryDto();
        insurantName.setClientName(value);
        final ClaimInfoQueryDto contractNumber = new ClaimInfoQueryDto();
        contractNumber.setContractNumber(value);
        final ClaimInfoQueryDto insuredObject = new ClaimInfoQueryDto();
        insuredObject.setInsObject(value);
        final ClaimInfoQueryDto victimLicensePlateNumber = new ClaimInfoQueryDto();
        victimLicensePlateNumber.setInjuredObject(value);
        final ClaimInfoQueryDto vinCode = new ClaimInfoQueryDto();
        vinCode.setInsObject(value);
        final List<ClaimInfoDto> searchResult = Stream.of(insuranceCaseNumber,
                                                          insuranceCaseNotificationNumber,
                                                          victimName,
                                                          insurantName,
                                                          contractNumber,
                                                          insuredObject,
                                                          victimLicensePlateNumber,
                                                          vinCode)
                                                          .parallel()
                                                          .map(searchService::listClaimInfos)
                                                          .flatMap(page -> page.getList().stream())
                                                          .collect(Collectors.toList());
        return ResponseEntity.ok(searchResult);
    }

    @PutMapping(value = ROOT + "/contracts")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"chatbot\")")
    public Page<ContractInfoDto> searchContracts(@RequestBody final ContractInfoQueryDto query) {
        return searchService.searchContracts(query);
    }

    /**
     * simple lightweight search for bot
     * */
    @GetMapping(value = ROOT + "/notifications/exist", produces = TEXT_PLAIN_VALUE)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"chatbot\")")
    public ResponseEntity<String> notificationExist(@RequestParam("number") @NotBlank final String number) {
        final ClaimInfoQueryDto insuranceCaseNotificationNumber = new ClaimInfoQueryDto();
        insuranceCaseNotificationNumber.setNoticeNumber(number);
        final Page<ClaimInfoDto> claimInfoDtoPage = searchService.listClaimInfos(insuranceCaseNotificationNumber);
        final List<ClaimInfoDto> claims = claimInfoDtoPage.getTotalCount() > 0 ? claimInfoDtoPage.getList() : Collections.emptyList();
        final ClaimInfoDto claimInfoDto = claims.stream()
                .filter(r -> number.equals(r.getNoticeNumber()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("notification does not exist"));
        return ResponseEntity.ok(String.valueOf(claimInfoDto.getNoticeId()));
    }

}
