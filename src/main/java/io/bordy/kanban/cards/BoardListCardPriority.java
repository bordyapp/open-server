package io.bordy.kanban.cards;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BoardListCardPriority {

    @JsonProperty("no_priority")
    NO_PRIORITY,
    @JsonProperty("low")
    LOW,
    @JsonProperty("medium")
    MEDIUM,
    @JsonProperty("high")
    HIGH,
    @JsonProperty("urgent")
    URGENT

}
