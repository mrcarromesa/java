package br.com.mrcarromesa.todolist.users;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface IUserRepository extends JpaRepository<UserModel, UUID> {
  UserModel findByUserName(String userName);
}
