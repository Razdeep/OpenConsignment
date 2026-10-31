WITH cte_collected AS (
    SELECT
        `Bill No.`,
        sum(`Amount Collected`) AS amount_collected
    FROM Collection_Entry_Extended_Table
    GROUP BY `Bill No.`
),
cte_bill AS (
    SELECT
        `Bill No.`,
        `Bill Date`,
        `Bill Amount`,
        `Supplier Name`
    FROM Bill_Entry_Table
    WHERE `Buyer Name` = ?
),
cte_bill_collected AS (
    SELECT
        `Bill No.`,
        `Bill Date`,
        `Bill Amount`,
        `Supplier Name`,
        coalesce(`amount_collected`, 0) AS amount_collected
    FROM cte_bill
    LEFT JOIN cte_collected USING(`Bill No.`)
),
cte_pending AS (
    SELECT
        `Bill No.`,
        `Bill Date`,
        `Bill Amount`,
        `Supplier Name`,
        `Bill Amount` - amount_collected AS pending_amount
    FROM cte_bill_collected
),
cte_final AS (
    SELECT
        `Bill No.`,
        `Bill Date`,
        `Bill Amount`,
        `Supplier Name`
    FROM cte_pending
    WHERE pending_amount > 0
    ORDER BY 1
)
SELECT * FROM cte_final;
