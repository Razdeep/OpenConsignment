INSERT INTO `Payment_Entry_Table`(`Voucher No.`,`Voucher Date`,`Supplier Name`,`Total Amount`)
VALUES (?,?,?,?)
ON CONFLICT(`Voucher No.`) DO UPDATE SET
    `Voucher Date` = excluded.`Voucher Date`,
    `Supplier Name` = excluded.`Supplier Name`,
    `Total Amount` = excluded.`Total Amount`;
