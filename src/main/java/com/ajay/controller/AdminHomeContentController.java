package com.ajay.controller;

import com.ajay.domains.HomeSectionKey;
import com.ajay.payload.request.HomeContentItemRequest;
import com.ajay.payload.response.HomeContentItemResponse;
import com.ajay.payload.response.HomeSectionConfigResponse;
import com.ajay.service.HomeContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/home-content")
@RequiredArgsConstructor
public class AdminHomeContentController {

    private final HomeContentService homeContentService;

    @GetMapping("/items")
    public ResponseEntity<List<HomeContentItemResponse>> getItems(
            @RequestParam(required = false) HomeSectionKey sectionKey,
            @RequestParam(defaultValue = "false") boolean onlyActive
    ) {
        return ResponseEntity.ok(homeContentService.getItems(sectionKey, onlyActive));
    }

    @PostMapping("/items")
    public ResponseEntity<HomeContentItemResponse> createItem(@Valid @RequestBody HomeContentItemRequest request) {
        return ResponseEntity.ok(homeContentService.createItem(request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<HomeContentItemResponse> updateItem(@PathVariable Long id,
                                                         @Valid @RequestBody HomeContentItemRequest request) {
        return ResponseEntity.ok(homeContentService.updateItem(id, request));
    }

    @PatchMapping("/items/{id}/status")
    public ResponseEntity<HomeContentItemResponse> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(homeContentService.updateStatus(id, active));
    }

    @PatchMapping("/items/{id}/order")
    public ResponseEntity<HomeContentItemResponse> updateOrder(@PathVariable Long id, @RequestParam Integer displayOrder) {
        return ResponseEntity.ok(homeContentService.updateDisplayOrder(id, displayOrder));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        homeContentService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sections")
    public ResponseEntity<List<HomeSectionConfigResponse>> getSections() {
        return ResponseEntity.ok(homeContentService.getSectionConfigs());
    }

    @PutMapping("/sections/{sectionKey}")
    public ResponseEntity<HomeSectionConfigResponse> updateSection(@PathVariable HomeSectionKey sectionKey,
                                                              @RequestBody HomeSectionConfigResponse request) {
        return ResponseEntity.ok(homeContentService.updateSectionConfig(sectionKey, request));
    }
}

