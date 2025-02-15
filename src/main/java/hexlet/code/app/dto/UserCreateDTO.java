package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCreateDTO {

    @Email
    @NotBlank
    private String email;

    private String firstName;
    private String lastName;

    @Size(min = 3, max = 100)
    @NotBlank
    private String password;
}
