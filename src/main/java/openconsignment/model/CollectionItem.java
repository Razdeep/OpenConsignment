/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openconsignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class CollectionItem {
    private String billNo;
    private String billDate;
    private String billAmount;
    private String supplierName;
    private String due;
    private String amountCollected;
    private String bank;
    private String ddNo;
    private String ddDate;
}
