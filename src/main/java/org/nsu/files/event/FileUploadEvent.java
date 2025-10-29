package org.nsu.files.event;

import lombok.Getter;
import org.nsu.files.dto.MetadataDTO;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class FileUploadEvent extends ApplicationEvent {

    private final List<MultipartFile> files;
    private final MetadataDTO metadata;
    private final Long userId;
    private final Long adId;

    public FileUploadEvent(Object source, List<MultipartFile> files, MetadataDTO metadata, Long userId, Long adId) {
        super(source);
        this.files = files;
        this.metadata = metadata;
        this.userId = userId;
        this.adId = adId;
    }
}
