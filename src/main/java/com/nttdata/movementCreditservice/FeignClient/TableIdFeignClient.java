package com.nttdata.movementCreditservice.FeignClient;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nttdata.movementCreditservice.FeignClient.FallBackImpl.TableIdFeignClientFallBack;
import com.nttdata.movementCreditservice.model.TableId;

@FeignClient(name="tableIdFeignClient",url="${api.tableId-service.uri}", fallback = TableIdFeignClientFallBack.class)
public interface TableIdFeignClient {

	@GetMapping("/generateKey/{nameTable}")
	TableId tableIdFindId(@PathVariable("nameTable") String nameTable);
}
