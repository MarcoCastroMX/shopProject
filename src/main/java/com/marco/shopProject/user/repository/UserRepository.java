package com.marco.shopProject.user.repository;

import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    Optional<List<User>> findUserByRoles(RolesEnum rol);
}
