package com.hexaware.fastx.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "bus_operators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusOperator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long operatorId;

    private String companyName;

    private String email;

    private String password;

    private String phoneNumber;

    private String address;

    private Boolean isActive = false;

    @OneToMany(mappedBy = "busOperator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Bus> buses;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
