package br.com.fiap.insanetask.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import br.com.fiap.insanetask.user.User;
import br.com.fiap.insanetask.user.UserService;

@Service
public class TaskService {

    @Autowired
    TaskRepository repository;

    @Autowired
    UserService userService;

    public List<Task> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void create(Task task) {
        repository.save(task);
    }

    public void decrement(Long id) {
        var optional = repository.findById(id);
        if (optional.isEmpty())
            throw new RuntimeException("tarefa não encontrada");

        var task = optional.get();
        if (task.getStatus() == 0)
            throw new RuntimeException("status não pode ser negativo");

        task.setStatus(task.getStatus() - 10);
        repository.save(task);
    }

    public void increment(Long id) {
        var optional = repository.findById(id);
        if (optional.isEmpty())
            throw new RuntimeException("tarefa não encontrada");

        var task = optional.get();
        if (task.getStatus() == 100)
            throw new RuntimeException("status não pode ser maior do que 100%");

        task.setStatus(task.getStatus() + 10);

        if (task.getStatus() == 100){
            var user = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userService.addScore(User.convert(user), task.getScore());
        }


        repository.save(task);
    }

    public void catchTask(Long id, User user) {
        var optional = repository.findById(id);
        if (optional.isEmpty())
            throw new RuntimeException("tarefa não encontrada");

        var task = optional.get();

        if (task.getUser() != null)
            throw new RuntimeException("tarefa já atribuída");

        task.setUser(user);
        repository.save(task);

    }

    public void dropTask(Long id, User user) {
        var optional = repository.findById(id);
        if (optional.isEmpty())
            throw new RuntimeException("tarefa não encontrada");

        var task = optional.get();

        if (task.getUser() == null)
            throw new RuntimeException("tarefa não já atribuída");

        if (!task.getUser().equals(user))
            throw new RuntimeException("tarefa atribuída para outro usuário");

        task.setUser(null);
        repository.save(task);
    }

}
