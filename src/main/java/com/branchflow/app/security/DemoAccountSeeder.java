package com.branchflow.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoAccountSeeder {
    @Bean
    ApplicationRunner seedDemoAccounts(UserAccountRepository users, PasswordEncoder encoder,
            @Value("${branchflow.security.admin.username:admin}") String adminName,
            @Value("${branchflow.security.admin.password:}") String adminPassword,
            @Value("${branchflow.security.manager.username:manager}") String managerName,
            @Value("${branchflow.security.manager.password:}") String managerPassword,
            @Value("${branchflow.security.employee.username:employee}") String employeeName,
            @Value("${branchflow.security.employee.password:}") String employeePassword) {
        return args -> {
            seed(users, encoder, adminName, adminPassword, UserRole.ADMIN);
            seed(users, encoder, managerName, managerPassword, UserRole.MANAGER);
            seed(users, encoder, employeeName, employeePassword, UserRole.EMPLOYEE);
        };
    }

    private void seed(UserAccountRepository users, PasswordEncoder encoder, String username,
                      String password, UserRole role) {
        if (password == null || password.length() < 12) {
            throw new IllegalStateException("Set a unique " + role.name().toLowerCase() +
                    " password of at least 12 characters before starting BranchFlow");
        }
        if (!users.existsByUsername(username)) {
            users.save(new UserAccount(username, encoder.encode(password), role));
        }
    }
}
