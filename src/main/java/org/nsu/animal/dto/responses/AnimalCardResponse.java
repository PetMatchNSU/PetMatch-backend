package org.nsu.animal.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Ответ с данными карточки животного")
public class AnimalCardResponse {

    @Nonnull
    @Schema(description = "Может ли пользователь редактировать карточку", example = "true")
    private Boolean canEdit;

    @Nonnull
    @Schema(description = "Кличка животного", example = "Барсик")
    private String name;

    @Nonnull
    @Schema(description = "Информация о виде животного")
    private SpeciesDto species;

    @Nonnull
    @Schema(description = "Цель размещения", example = "SELL", allowableValues = {"SELL", "BREEDING", "GIVE_AWAY"})
    private String goal;

    @Nullable
    @Schema(description = "Стоимость продажи", example = "15000")
    private BigDecimal cost;

    @Nonnull
    @Schema(description = "Флаг породистости", example = "true")
    private Boolean hasBreed;

    @Nullable
    @Schema(description = "Название породы", example = "Британская короткошерстная")
    private String breed;

    @Nonnull
    @Schema(description = "Пол животного", example = "M", allowableValues = {"M", "F"})
    private String gender;

    @Nonnull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата рождения", example = "2023-11-15")
    private LocalDate birthday;

    @Nullable
    @Schema(description = "Вес животного в кг", example = "5.2")
    private BigDecimal weight;

    @Nonnull
    @Schema(description = "Описание окраса", example = "Серый с белыми пятнами")
    private String color;

    @Nonnull
    @Schema(description = "Наследственные заболевания", example = "Наследственных заболеваний не выявлено")
    private String geneticDiseases;

    @Nullable
    @Schema(description = "Описание животного", example = "Очень дружелюбный и игривый кот")
    private String description;

    @Nonnull
    @Schema(description = "Статус карточки", example = "PUBLISHED", allowableValues = {"ON_CHECKING", "PUBLISHED", "BLOCKED"})
    private String reviewStatus;

    @Nonnull
    @Schema(description = "Информация о фотографиях")
    private PhotosDto photos;

    @Nonnull
    @Schema(description = "Информация о документах")
    private DocumentsDto documents;

    @Nonnull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Schema(description = "Дата создания", example = "2023-11-15T12:00:00Z")
    private LocalDateTime createdAt;

    @Nonnull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Schema(description = "Дата обновления", example = "2023-11-15T12:00:00Z")
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(description = "Информация о виде животного")
    public static class SpeciesDto {
        
        @Nonnull
        @Schema(description = "Идентификатор вида", example = "1")
        private Long id;

        @Nonnull
        @Schema(description = "Название вида", example = "Кошка")
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(description = "Информация о фотографиях")
    public static class PhotosDto {
        
        @Nullable
        @Schema(description = "Идентификатор основного фото", example = "1324325")
        private Long mainPhotoId;

        @Nonnull
        @Schema(description = "Идентификаторы дополнительных фото")
        private List<Long> additionalIds;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(description = "Информация о документах")
    public static class DocumentsDto {
        
        @Nullable
        @Schema(description = "Идентификатор ветеринарного паспорта", example = "34355")
        private Long vetPassportId;

        @Nullable
        @Schema(description = "Идентификатор родословной/метрики", example = "34356")
        private Long pedigreeId;

        @Nullable
        @Schema(description = "Идентификатор ветеринарной справки", example = "34357")
        private Long vetCertificatesId;

        @Nullable
        @Schema(description = "Идентификатор дипломов", example = "34358")
        private Long diplomasId;

        @Nullable
        @Schema(description = "Идентификатор других документов")
        private Long otherDocumentsId;
    }
}