package io.bordy.storage;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class CloudStorage {

    @Inject
    Storage storage;

    @Nonnull
    public String uploadWorkspacePhoto(@Nonnull FileUpload image, @Nonnull String workspaceId) throws IOException {
        BlobInfo blobInfo = BlobInfo.newBuilder(
                        "bordy",
                        "workspace-photo/" + workspaceId
                ).setAcl(
                        List.of(
                                Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)
                        )
                ).setContentType(image.contentType())
                .setCacheControl("public, max-age=300, must-revalidate")
                .build();
        var result = storage.createFrom(blobInfo, image.uploadedFile());
        return "https://storage.googleapis.com/bordy/workspace-photo/" + workspaceId;
    }

}
