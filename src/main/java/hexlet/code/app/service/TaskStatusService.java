package hexlet.code.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO;
import hexlet.code.app.dto.TaskStatusUpdateDTO;
import hexlet.code.app.exception.DuplicateSlugException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskStatusRepository;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    public List<TaskStatusDTO> getAll() {
        var taskStatuses = taskStatusRepository.findAll();

        return taskStatuses.stream()
            .map(taskStatusMapper::map)
            .toList();
    }

    public TaskStatusDTO findById(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));

        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO data) {
        var slug = data.getSlug();

        if (taskStatusRepository.existsBySlug(slug)) {
            throw new DuplicateSlugException("Duplicate Slug: " + slug);
        }

        var taskStatus = taskStatusMapper.map(data);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO data, Long id) {
        var taskStatus = taskStatusRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));

        taskStatusMapper.update(data, taskStatus);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public void delete(Long id) {
        taskStatusRepository.deleteById(id);
    }
}