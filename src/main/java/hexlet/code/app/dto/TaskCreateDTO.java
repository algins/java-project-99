package hexlet.code.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskCreateDTO {
    private Integer index;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @NotBlank
    @JsonProperty("title")
    private String name;

    @JsonProperty("content")
    private String description;

    @NotNull
    private String status;

    private List<Long> labelIds;
}
