package com.nttdata.movementCreditservice.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
//@Document(collection = "configuration")
public class Configuration {
	//@Id
	private Long idConfiguration;
	private Double costMaintenance;// Costo de mantenimiento
	private Integer quantityMovement;// Total de movimientos
	private String specificDate;
	private Integer quantityCredit;// Cantidad de movimientos permitidos, si solo permite un dia de moviento se
						// especifica fecha
	private Double minOpeningAmount;
	private Double minDayAverageAmount;
	private Double transactionFee;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss") 
	private Date creationDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
	private Date dateModified;
 
	
	
}
