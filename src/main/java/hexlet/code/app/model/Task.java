package hexlet.code.app.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.LocalDate;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tasks")
public class Task implements BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Integer index;

    @ManyToOne
    private User assignee;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @ManyToOne
    private TaskStatus taskStatus;

    @CreatedDate
    private LocalDate createdAt;
}
