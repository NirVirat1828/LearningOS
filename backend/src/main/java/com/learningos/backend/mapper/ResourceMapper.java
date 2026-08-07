package com.learningos.backend.mapper;

import com.learningos.backend.dto.ResourceResponse;
import com.learningos.backend.entity.Resource;

public final class ResourceMapper {

    private ResourceMapper() {
    }

    public static ResourceResponse toResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getTopic().getId(),
                resource.getTitle(),
                resource.getUrl(),
                resource.getType(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
