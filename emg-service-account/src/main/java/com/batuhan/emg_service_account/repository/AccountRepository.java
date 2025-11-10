package com.batuhan.emg_service_account.repository;

import com.batuhan.emg_service_account.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByUsername(String username);
    Optional<AccountEntity> findByEmail(String email);
    boolean existsByUsernameOrEmail(String username, String email);
    Optional<AccountEntity> findByIdAndIsAccountActive(Long id, boolean isAccountActive);
}