SELECT * FROM `Bill_Entry_Table`
LEFT JOIN (
    `Collection_Entry_Extended_Table`
    JOIN `Collection_Entry_Table` USING ("Voucher No.")
) USING("Bill No.")
WHERE `Bill_Entry_Table`.`Buyer Name`=?
