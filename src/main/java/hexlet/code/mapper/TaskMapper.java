package hexlet.code.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;

@Mapper(
    uses = {JsonNullableMapper.class, ReferenceMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "status", target = "taskStatus", qualifiedByName = "slugToTaskStatus")
    @Mapping(source = "taskLabelIds", target = "labels", qualifiedByName = "taskLabelIdsToLabels")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "taskStatus.slug", target = "status")
    @Mapping(source = "labels", target = "taskLabelIds", qualifiedByName = "labelsTotaskLabelIds")
    public abstract TaskDTO map(Task model);

    @Mapping(source = "assigneeId", target = "assignee.id")
    @Mapping(source = "status", target = "taskStatus.slug")
    @Mapping(source = "taskLabelIds", target = "labels", qualifiedByName = "taskLabelIdsToLabels")
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @Named("slugToTaskStatus")
    public TaskStatus slugToTaskStatus(String slug) {
        return taskStatusRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + slug));
    }

    @Named("taskLabelIdsToLabels")
    public List<Label> taskLabelIdsToLabels(List<Long> taskLabelIds) {
        return taskLabelIds == null ? null : taskLabelIds.stream()
            .map(id -> labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found: " + id)))
            .toList();
    }

    @Named("labelsTotaskLabelIds")
    public List<Long> labelsTotaskLabelIds(List<Label> labels) {
        return labels == null ? null : labels.stream()
            .map(Label::getId)
            .toList();
    }
}
