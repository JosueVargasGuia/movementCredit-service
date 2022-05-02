package com.nttdata.movementCreditservice.FeignClient.FallBackImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nttdata.movementCreditservice.FeignClient.ConfigurationFeingClient;
import com.nttdata.movementCreditservice.model.Configuration;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ConfigurationFeingClientFallback implements ConfigurationFeingClient {
	@Value("${api.configuration-service.uri}")
	String configService;

	public  Configuration  findById(Long idConfiguration) {
		log.info("ConfigurationFeingClientFallback findAll[" + configService + "]");
		return null;
	}

 

}
