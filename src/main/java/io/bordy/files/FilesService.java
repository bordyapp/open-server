package io.bordy.files;

import io.bordy.gateways.files.ReadFileDto;
import io.bordy.gateways.files.UpdateFileDto;
import io.bordy.gateways.files.WriteFileDto;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class FilesService {

    public List<ReadFileDto> find(@Nonnull UUID workspaceId) {
        return File.<File>find("workspaceId", workspaceId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Transactional
    public ReadFileDto create(
            @Nonnull UUID workspaceId,
            @Nonnull String userId,
            @Nonnull WriteFileDto writeFileDto
    ) {
        var createdAt = new Date();
        var file = new File(
                UUID.randomUUID(),
                writeFileDto.name(),
                workspaceId,
                null,
                writeFileDto.type(),
                writeFileDto.url(),
                userId,
                createdAt,
                createdAt
        );
        file.persist();

        return toDto(file);
    }

    @CheckForNull
    @Transactional
    public ReadFileDto update(
            @Nonnull UUID workspaceId,
            @Nonnull UUID fileId,
            @Nonnull String uerId,
            @Nonnull UpdateFileDto writeFileDto
    ) {
        var file = File.<File>find(
                "_id = ?1 and workspaceId = ?2",
                fileId, workspaceId
        ).firstResult();
        if (file == null) {
            return null;
        }

        file.setName(writeFileDto.name());
        file.setUrl(writeFileDto.url());
        file.setEditedAt(new Date());
        file.update();
        return toDto(file);
    }

    public void delete(
            @Nonnull UUID workspaceId,
            @Nonnull UUID fileId,
            @Nonnull String uerId
    ) {
        File.delete(
                "_id = ?1 and workspaceId = ?2",
                fileId, workspaceId
        );
    }

    public ReadFileDto toDto(File file) {
        return new ReadFileDto(
                file.getId(),
                file.getName(),
                file.getWorkspaceId(),
                file.getFolderId(),
                file.getType(),
                file.getUrl(),
                file.getCreatorUserId(),
                file.getCreatedAt(),
                file.getEditedAt()
        );
    }

}
