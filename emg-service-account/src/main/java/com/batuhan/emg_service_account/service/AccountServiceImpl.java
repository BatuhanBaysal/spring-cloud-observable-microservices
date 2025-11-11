package com.batuhan.emg_service_account.service;

import com.batuhan.emg_service_account.entity.AccountEntity;
import com.batuhan.emg_service_account.exception.DuplicateResourceException;
import com.batuhan.emg_service_account.exception.ResourceNotFoundException;
import com.batuhan.emg_service_account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService, UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountEntity account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return account;
    }

    @Override
    @Transactional
    public AccountEntity createAccount(AccountEntity accountEntity) {
        prepareNewAccountData(accountEntity);
        checkAccountUniqueness(accountEntity);
        setCreationDefaults(accountEntity);
        return accountRepository.save(accountEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountEntity getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "ID", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountEntity getAccountByUsername(String username) {
        String cleanedUsername = username.trim().toLowerCase();
        return accountRepository.findByUsername(cleanedUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "Username", username));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountEntity getAccountByEmail(String email) {
        String cleanedEmail = email.trim().toLowerCase();
        return accountRepository.findByEmail(cleanedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "Email", email));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountEntity getActiveAccountById(Long id) {
        return accountRepository.findByIdAndIsAccountActive(id, true)
                .orElseThrow(() -> new ResourceNotFoundException("Active Account", "ID", id));
    }

    @Override
    @Transactional
    public AccountEntity updateAccount(Long id, AccountEntity updatedAccount) {
        AccountEntity existingAccount = getAccountById(id);
        prepareUpdateAccountData(existingAccount, updatedAccount);
        return accountRepository.save(existingAccount);
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        AccountEntity accountToDelete = getAccountById(id);
        accountRepository.delete(accountToDelete);
    }


    private void prepareNewAccountData(AccountEntity accountEntity) {
        String plainPassword = accountEntity.getPasswordHash();
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("The password field cannot be left blank.");
        }

        accountEntity.setUsername(accountEntity.getUsername().trim().toLowerCase());
        accountEntity.setEmail(accountEntity.getEmail().trim().toLowerCase());
        accountEntity.setFirstName(accountEntity.getFirstName().trim());
        accountEntity.setLastName(accountEntity.getLastName().trim());

        String hashedPassword = passwordEncoder.encode(plainPassword);
        accountEntity.setPasswordHash(hashedPassword);
    }

    private void checkAccountUniqueness(AccountEntity accountEntity) {
        if (accountRepository.existsByUsernameOrEmail(accountEntity.getUsername(), accountEntity.getEmail())) {
            throw new DuplicateResourceException("Account", "Username or Email", accountEntity.getUsername() + " / " + accountEntity.getEmail());
        }
    }

    private void setCreationDefaults(AccountEntity accountEntity) {
        accountEntity.setAccountActive(true);
    }

    private void prepareUpdateAccountData(AccountEntity existingAccount, AccountEntity updatedAccount) {
        String newUsername = updatedAccount.getUsername().trim().toLowerCase();
        String newEmail = updatedAccount.getEmail().trim().toLowerCase();

        boolean usernameChanged = !newUsername.equals(existingAccount.getUsername());
        boolean emailChanged = !newEmail.equals(existingAccount.getEmail());

        if (usernameChanged || emailChanged) {
            if (accountRepository.existsByUsernameOrEmail(newUsername, newEmail)) {
                throw new DuplicateResourceException("Account", "Username or Email", newUsername + " / " + newEmail);
            }
        }

        existingAccount.setUsername(newUsername);
        existingAccount.setEmail(newEmail);
        existingAccount.setFirstName(updatedAccount.getFirstName().trim());
        existingAccount.setLastName(updatedAccount.getLastName().trim());

        String newPlainPassword = updatedAccount.getPasswordHash();
        if (newPlainPassword != null && !newPlainPassword.isEmpty()) {
            existingAccount.setPasswordHash(passwordEncoder.encode(newPlainPassword));
        }
    }
}