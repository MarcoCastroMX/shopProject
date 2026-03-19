package com.marco.shopProject.auth.repository;

import com.marco.shopProject.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findAllExpiredIsFalseOrRevokedIsFalseByUserId(Long id);
    Token findByToken(String jwtToken);
}
