package com.aystream.smsw.core.trade;

import com.aystream.smsw.core.orderbook.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buy_order_id", referencedColumnName = "id")
    private Order buyOrder;

    @ManyToOne
    @JoinColumn(name = "sell_order_id", referencedColumnName = "id")
    private Order sellOrder;

    @NotNull
    private String symbol;

    @NotNull
    @Min(1)
    private Integer price;

    @NotNull
    private Integer quantity;

    @NotNull
    private Date timestamp;
}
