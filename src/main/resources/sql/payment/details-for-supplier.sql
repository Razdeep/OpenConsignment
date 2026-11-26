WITH cte_payment_partial AS (SELECT `Bill No.`,
                                    sum(`Amount Paid`) AS amount_paid
                             FROM Payment_Entry_Extended_Table
                             GROUP BY `Bill No.`),

     cte_payment_full AS (SELECT `Bill No.`,
                                 `Bill Date`,
                                 `Bill Amount`,
                                 `Supplier Name`,
                                 `Buyer Name`,
                                 coalesce(amount_paid, 0) as amount_paid
                          from `Bill_Entry_Table`
                                   left join cte_payment_partial using ("Bill No.")),

     cte_payment_joined as (SELECT *
                            from `Payment_Entry_Extended_Table`
                                     JOIN `Payment_Entry_Table` USING ("Voucher No.")),

     cte_final AS (SELECT *, cte_payment_full.`Bill Amount` - amount_paid AS pending_amount
                   FROM cte_payment_full
                            LEFT JOIN cte_payment_joined USING ("Bill No.")
                   WHERE cte_payment_full.`Supplier Name` = ?)

SELECT *
FROM cte_final;
