package owner.backflow.service;

import owner.backflow.config.AppAdminProperties;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    private final String username;
    private final String password;

    public AdminAuthService(AppAdminProperties adminProperties) {
        this.username = adminProperties.username();
        this.password = adminProperties.password();
    }

    public boolean authenticate(String candidateUsername, String candidatePassword) {
        if (!isConfigured()) {
            return false;
        }
        return username.equals(candidateUsername) && password.equals(candidatePassword);
    }

    public String username() {
        return username;
    }

    public boolean isConfigured() {
        return username != null
                && !username.isBlank()
                && password != null
                && !password.isBlank();
    }
}
