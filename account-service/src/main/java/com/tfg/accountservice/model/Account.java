package com.tfg.accountservice.model;

/**************************************************************************
   this class represents a production received from Production Service  
***************************************************************************/

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@Entity
@Table(name = "stockEntries")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productionId;
    private int amount;
}
