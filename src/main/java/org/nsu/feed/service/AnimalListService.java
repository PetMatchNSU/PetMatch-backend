package org.nsu.feed.service;

import lombok.RequiredArgsConstructor;
import org.nsu.feed.dto.requests.animalList.AnimalListRequest;
import org.nsu.feed.dto.responses.animalList.AnimalListResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnimalListService {
    private final AnimalCardRetrieverService animalCardRetriteverService;

    public AnimalListResponse listAnimals(AnimalListRequest dto) {
        AnimalListResponse response = new AnimalListResponse();

        long page = 1L;
        long pageLimit = 20L;
        if (dto.getPagination() != null) {
            if (dto.getPagination().getPage() != null)
                page = dto.getPagination().getPage();
            if (dto.getPagination().getLimit() != null)
                pageLimit = dto.getPagination().getLimit();
        }

        var pageResult = animalCardRetriteverService.getList(dto.getFilter(), page, pageLimit);
        response.setAnimalList(pageResult.getContent());

        AnimalListResponse.Pagination pagination = new AnimalListResponse.Pagination();
        pagination.setCurrentPage(page);
        pagination.setPageSize(pageLimit);
        long totalItems = pageResult.getTotalElements();
        pagination.setTotalItems(totalItems);
        long totalPages = (long) Math.ceil((double) totalItems / (double) pageLimit);
        pagination.setTotalPages(totalPages);
        pagination.setHasNextPage(page < totalPages);
        pagination.setHasPreviousPage(page > 1);
        response.setPagination(pagination);

        return response;
    }
}
