package io.bordy.kanban.cards;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BoardListCardStatus {

    @JsonProperty("not_started")
    NOT_STARTED,
    @JsonProperty("in_progress")
    IN_PROGRESS,
    @JsonProperty("done")
    DONE,
    @JsonProperty("closed")
    CLOSED

}
