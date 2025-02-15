package hexlet.code.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.DuplicateNameException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;

@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    public List<LabelDTO> getAll() {
        var labels = labelRepository.findAll();

        return labels.stream()
            .map(labelMapper::map)
            .toList();
    }

    public LabelDTO findById(Long id) {
        var label = labelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));

        return labelMapper.map(label);
    }

    public LabelDTO create(LabelCreateDTO data) {
        var name = data.getName();

        if (labelRepository.existsByName(name)) {
            throw new DuplicateNameException("Duplicate Name: " + name);
        }

        var label = labelMapper.map(data);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO update(LabelUpdateDTO data, Long id) {
        var label = labelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));

        labelMapper.update(data, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public void delete(Long id) {
        labelRepository.deleteById(id);
    }
}
