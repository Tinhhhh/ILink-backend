package com.exe201.ilink.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "voucher")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long id;

    @JsonProperty("min_value")
    @Column(name = "min_value")
    private double minValue;

    @JsonProperty("max_value")
    @Column(name = "max_value")
    private double max_value;

    @JsonProperty("percentage_value")
    @Column(name = "percentage_value")
    private int percentageValue;

    @Column(name = "quantity")
    private int quantity;

    @JsonProperty("is_enable")
    @Column(name = "is_Enable")
    private boolean isEnable;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @JsonIgnore
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @JsonProperty("expired_date")
    @Column(name = "expired_date")
    private Date ExpiredDate;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CustomerOrder> orders;


}
