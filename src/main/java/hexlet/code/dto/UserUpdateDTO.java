package hexlet.code.dto;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserUpdateDTO {

    @Email
    @NotBlank
    private JsonNullable<String> email;

    private JsonNullable<String> firstName;
    private JsonNullable<String> lastName;

    @Size(min = 3, max = 100)
    @NotBlank
    private JsonNullable<String> password;
}
