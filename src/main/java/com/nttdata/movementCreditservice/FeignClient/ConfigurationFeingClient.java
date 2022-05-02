package com.nttdata.movementCreditservice.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nttdata.movementCreditservice.FeignClient.FallBackImpl.ConfigurationFeingClientFallback;
import com.nttdata.movementCreditservice.model.Configuration;

@FeignClient(name = "${api.configuration-service.uri}", fallback = ConfigurationFeingClientFallback.class)
public interface ConfigurationFeingClient {
	@GetMapping("/{idConfiguration}")
	Configuration findById(@PathVariable(name="idConfiguration") Long idConfiguration);
}
