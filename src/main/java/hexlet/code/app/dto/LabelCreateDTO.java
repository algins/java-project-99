package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LabelCreateDTO {

    @Size(min = 3, max = 1000)
    @NotBlank
    private String name;
}
