INSERT INTO `Bill_Entry_Table`(
    `Supplier Name`, `Buyer Name`, `Bill No.`, `Bill Date`, `Transport`,
    `LR Date`, `Bill Amount`, `Collection Due`, `Due`
)
VALUES (?,?,?,?,?,?,?,?,?)
ON CONFLICT(`Bill No.`) DO UPDATE SET
    `Supplier Name` = excluded.`Supplier Name`,
    `Buyer Name` = excluded.`Buyer Name`,
    `Bill Date` = excluded.`Bill Date`,
    `Transport` = excluded.`Transport`,
    `LR Date` = excluded.`LR Date`,
    `Bill Amount` = excluded.`Bill Amount`,
    `Collection Due` = excluded.`Collection Due`,
    `Due` = excluded.`Due`;
