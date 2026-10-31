INSERT INTO `Collection_Entry_Table`(`Voucher No.`,`Voucher Date`,`Buyer Name`,`Total Amount`)
VALUES (?,?,?,?)
ON CONFLICT(`Voucher No.`) DO UPDATE SET
    `Voucher Date` = excluded.`Voucher Date`,
    `Buyer Name` = excluded.`Buyer Name`,
    `Total Amount` = excluded.`Total Amount`;
