package io.bordy.gateways.comments;

public record CommentCreatorDto(
        String id,
        String nickname,
        String picture
) {
}