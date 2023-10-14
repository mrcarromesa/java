package br.com.mrcarromesa.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.mrcarromesa.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  
  @Autowired
  private ITaskRepository taskRepository;

  private String validationWithMsg(TaskModel taskModel) {
    var currentDate = LocalDateTime.now();
    
    if(currentDate.isAfter(taskModel.getStartAt())) {
      return "The startAt needs be more than current date";
    }

    if(taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return "The endAt needs be more than startAt";
    }

    return "";
  }

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) idUser);
    
    var validationMsg = validationWithMsg(taskModel); 
    if (!validationMsg.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationMsg);
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    var taskList = this.taskRepository.findByIdUser((UUID) idUser);

    return taskList;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    
    var taskToFind = this.taskRepository.findById(id).orElse(null); 

    if (taskToFind == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task not found");
    }

    var idUser = request.getAttribute("idUser");

    if (!taskToFind.getIdUser().equals(idUser)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This user doesn't have permission to update this tasks");
    }

    Utils.copyNonNullProperties(taskModel, taskToFind);

    this.taskRepository.save(taskToFind);

    return ResponseEntity.status(HttpStatus.OK).body(taskToFind);
  }
}
