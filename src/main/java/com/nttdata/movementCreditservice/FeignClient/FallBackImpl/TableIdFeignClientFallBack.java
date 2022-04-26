package com.nttdata.movementCreditservice.FeignClient.FallBackImpl;

import org.springframework.stereotype.Component;

import com.nttdata.movementCreditservice.FeignClient.TableIdFeignClient;
import com.nttdata.movementCreditservice.model.TableId;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TableIdFeignClientFallBack implements TableIdFeignClient{@Override
	
	public TableId tableIdFindId(String nameTable) {
		TableId tableId = new TableId();
		tableId.setNameTable(String.valueOf(Long.valueOf(-1)));
		//Long.parseLong(String.valueOf(tableId));
		log.info("TableIdFeignClientFallBack -> " + tableId);
		return tableId;
	}

	
}
