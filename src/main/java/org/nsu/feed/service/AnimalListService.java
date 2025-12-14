package org.nsu.feed.service;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.nsu.feed.dto.requests.AnimalListRequest;
import org.nsu.feed.dto.responses.animalList.AnimalListResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnimalListService {
    private final AnimalCardRetrieverService animalCardRetrieverService;

    public AnimalListResponse listAnimals(AnimalListRequest dto) {
        AnimalListResponse response = new AnimalListResponse();

        long page = Optional.ofNullable(dto.getPagination())
                .map(AnimalListRequest.Pagination::getPage)
                .orElse(1L);
        long pageLimit = Optional.ofNullable(dto.getPagination())
                .map(AnimalListRequest.Pagination::getLimit)
                .orElse(20L);

        var pageResult = animalCardRetrieverService.getList(page, pageLimit);
        response.setAnimalsList(pageResult.getContent());

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
