package com.marco.shopProject.identity.auth.repository;

import com.marco.shopProject.identity.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findAllExpiredIsFalseOrRevokedIsFalseByUserId(Long id);
    Token findByToken(String jwtToken);
}
