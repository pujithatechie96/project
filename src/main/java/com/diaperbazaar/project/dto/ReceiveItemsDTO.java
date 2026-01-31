package com.diaperbazaar.project.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReceiveItemsDTO {
    private List<ItemReceive> items;
    
    @Data
    public static class ItemReceive {
        private Long itemId;
        private Integer quantityReceived;
    }
}
