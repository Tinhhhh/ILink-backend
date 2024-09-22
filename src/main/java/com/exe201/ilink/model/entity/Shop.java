package com.exe201.ilink.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shop")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "description")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "reputation")
    private int reputation;

    @JsonProperty("is_locked")
    @Column(name = "is_locked")
    private boolean isLocked;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;


}
