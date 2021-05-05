package com.universalna.nsds.controller.amqp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class InsuranceCaseCreatedEventDto {

   @NotNull
   private Long settlementCaseId;

   @NotNull
   private Long noticeId;

}
