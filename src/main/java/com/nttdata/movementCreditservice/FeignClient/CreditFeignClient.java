package com.nttdata.movementCreditservice.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nttdata.movementCreditservice.FeignClient.FallBackImpl.CreditFeignClientFallBack;
import com.nttdata.movementCreditservice.model.CreditAccount;


@FeignClient(name="${api.credit-service.uri}", fallback = CreditFeignClientFallBack.class)
public interface CreditFeignClient {

	@GetMapping("/{id}")
	CreditAccount creditfindById(@PathVariable(name = "id") Long id);
}
