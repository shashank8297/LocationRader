package com.location.rader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.location.rader.model.User;

@Repository
public interface UserRepositoty extends JpaRepository<User, Long> {

}
