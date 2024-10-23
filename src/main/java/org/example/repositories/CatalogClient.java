package org.example.repositories;

import org.example.dto.ItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "http://localhost:8081")
public interface CatalogClient {
    @GetMapping("/items/{itemId}")
    ItemResponse getItemById(@PathVariable("itemId") String itemId);
}
