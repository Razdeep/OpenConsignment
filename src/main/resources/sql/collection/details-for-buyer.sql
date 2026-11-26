WITH cte_collected_partial AS (SELECT `Bill No.`,
                                      sum(`Amount Collected`) AS amount_collected
                               FROM Collection_Entry_Extended_Table
                               GROUP BY `Bill No.`),

     cte_collected_full AS (SELECT `Bill No.`,
                                   `Bill Date`,
                                   `Bill Amount`,
                                   `Supplier Name`,
                                   `Buyer Name`,
                                   coalesce(amount_collected, 0) as amount_collected
                            from `Bill_Entry_Table`
                                     left join cte_collected_partial using ("Bill No.")),

     cte_collection_joined as (SELECT *
                               from `Collection_Entry_Extended_Table`
                                        JOIN `Collection_Entry_Table` USING ("Voucher No.")),

     cte_final AS (SELECT *, cte_collected_full.`Bill Amount` - amount_collected AS pending_amount
                   FROM cte_collected_full
                            LEFT JOIN cte_collection_joined USING ("Bill No.")
                   WHERE cte_collected_full.`Buyer Name` = ?)


SELECT *
FROM cte_final;

