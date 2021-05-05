package com.universalna.nsds.service.search.profitsoft;


/**
 * all of this was copy-pasted from profitsoft BOSearcher
 * */
public interface ProfitsoftSearchService {

  Page<ClaimInfoDto> listClaimInfos(ClaimInfoQueryDto queryDto);

  Page<ContractInfoDto> searchContracts(ContractInfoQueryDto query);

  ClaimInfoDto getByNoticeId(Long noticeId);

  ClaimInfoDto getBySettlementCaseId(Long id);
}
