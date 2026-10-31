SELECT `LR No.`,`PM`,`Bill No.`,`Supplier Name`,`Buyer Name`,`LR Date`
FROM `LR_Table` JOIN `Bill_Entry_Table` USING (`Bill No.`)
WHERE `Transport`=?
AND substr(`LR Date`, 7, 4)||substr(`LR Date`, 4, 2)||substr(`LR Date`, 1, 2)
    BETWEEN substr(?, 7, 4)||substr(?, 4, 2)||substr(?, 1, 2)
    AND substr(?, 7, 4)||substr(?, 4, 2)||substr(?, 1, 2)
ORDER BY datetime(substr(`LR Date`, 7, 4)||'-'||substr(`LR Date`, 4, 2)||'-'||substr(`LR Date`, 1, 2))
