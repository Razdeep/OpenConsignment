WITH cte_paid AS (
    SELECT
        `Bill No.`,
        sum(`Amount Paid`) AS amount_paid
    FROM Payment_Entry_Extended_Table
    GROUP BY `Bill No.`
),
cte_bill AS (
    SELECT *
    FROM Bill_Entry_Table
    WHERE `Bill No.` = ?
),
cte_bill_paid AS (
    SELECT
        `Bill No.`,
        `Bill Amount`,
        coalesce(`amount_paid`, 0) AS amount_paid
    FROM cte_bill
    LEFT JOIN cte_paid USING(`Bill No.`)
),
cte_pending AS (
    SELECT
        `Bill No.`,
        `Bill Amount` - amount_paid AS pending_amount
    FROM cte_bill_paid
),
cte_final AS (
    SELECT pending_amount FROM cte_pending
)
SELECT * FROM cte_final;
