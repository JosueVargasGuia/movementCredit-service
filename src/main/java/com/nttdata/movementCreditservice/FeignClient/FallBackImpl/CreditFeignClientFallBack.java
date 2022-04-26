package com.nttdata.movementCreditservice.FeignClient.FallBackImpl;

import org.springframework.stereotype.Component;

import com.nttdata.movementCreditservice.FeignClient.CreditFeignClient;
import com.nttdata.movementCreditservice.model.Credit;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CreditFeignClientFallBack implements CreditFeignClient{

	@Override
	public Credit creditfindById(Long id) {
		Credit credit = new Credit();
		credit.setIdCredit(Long.valueOf(-1));
		log.info("CreditFeignClientFallBack ->" + credit);
		return credit;
	}

}
