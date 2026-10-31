WITH cte_collected AS (
    SELECT
        `Bill No.`,
        sum(`Amount Collected`) AS amount_collected
    FROM Collection_Entry_Extended_Table
    GROUP BY `Bill No.`
),
cte_bill AS (
    SELECT *
    FROM Bill_Entry_Table
    WHERE `Bill No.` = ?
),
cte_bill_collected AS (
    SELECT
        `Bill No.`,
        `Bill Amount`,
        coalesce(`amount_collected`, 0) AS amount_collected
    FROM cte_bill
    LEFT JOIN cte_collected USING(`Bill No.`)
),
cte_pending AS (
    SELECT
        `Bill No.`,
        `Bill Amount` - amount_collected AS pending_amount
    FROM cte_bill_collected
),
cte_final AS (
    SELECT pending_amount FROM cte_pending
)
SELECT * FROM cte_final;
