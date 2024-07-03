package hexlet.code.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder encoder;

    public List<UserDTO> getAll() {
        var users = userRepository.findAll();

        return users.stream()
            .map(userMapper::map)
            .toList();
    }

    public UserDTO findById(Long id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));

        return userMapper.map(user);
    }

    public UserDTO create(UserCreateDTO data) {
        var user = userMapper.map(data);
        var passwordDigest = encoder.encode(data.getPassword());
        user.setPasswordDigest(passwordDigest);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO update(UserUpdateDTO data, Long id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));

        userMapper.update(data, user);

        if (data.getPassword() != null) {
            var passwordDigest = encoder.encode(data.getPassword().get());
            user.setPasswordDigest(passwordDigest);
        }

        userRepository.save(user);
        return userMapper.map(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
