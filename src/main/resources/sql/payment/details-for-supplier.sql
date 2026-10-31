SELECT * FROM `Bill_Entry_Table`
LEFT JOIN (
    `Payment_Entry_Extended_Table`
    JOIN `Payment_Entry_Table` USING ("Voucher No.")
) USING("Bill No.")
WHERE `Bill_Entry_Table`.`Supplier Name`=?
ORDER BY `Bill_Entry_Table`.`Bill No.`
