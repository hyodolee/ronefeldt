package com.ronnefeldt.model;

import java.util.List;

public record StoreCategory(
    String key,
    String title,
    String path,
    List<String> bannerImages
) {

    public boolean hasBanner() {
        return bannerImages != null && !bannerImages.isEmpty();
    }
}
