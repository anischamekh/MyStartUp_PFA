package tn.iteam.backend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.ExperienceLevel;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Speciality;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.RoleRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.repository.UserRepository;

/**
 * Seeds demo users when {@code auth_db} has no users (matches {@code infra/postgres/seed/01-auth-minimal.sql}).
 * Default password for all accounts: {@code password}
 */
@Component
public class AuthDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthDataInitializer.class);
    private static final String DEMO_PASSWORD = "password";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;

    public AuthDataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            TeamRepository teamRepository,
            EmployeeProfileRepository employeeProfileRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:true}") boolean seedEnabled
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            return;
        }
        if (userRepository.count() > 0) {
            return;
        }

        log.info("auth_db has no users - seeding minimal accounts (password: {})", DEMO_PASSWORD);

        Arrays.stream(RoleName.values()).forEach(this::ensureRole);

        createUser("admin", "System Admin", "admin@mystartup.local", RoleName.ADMIN);
        createUser("hr", "HR User", "hr@mystartup.local", RoleName.HR);
        User manager = createUser("manager", "Project Manager", "manager@mystartup.local", RoleName.MANAGER);
        User employee = createUser("employee", "Employee User", "employee@mystartup.local", RoleName.EMPLOYEE);

        Team team = new Team();
        team.setName("Product Squad");
        team.setSpeciality(Speciality.FRONTEND);
        team.setTeamLeader(manager);
        team = teamRepository.save(team);

        createProfile(employee, team, 25, "Developer", ExperienceLevel.MID, new BigDecimal("3200.00"));

        log.info("Demo users ready: admin, hr, manager, employee - password '{}'", DEMO_PASSWORD);
    }

    private Role ensureRole(RoleName name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            return roleRepository.save(role);
        });
    }

    private User createUser(String username, String fullName, String email, RoleName roleName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(ensureRole(roleName));
        return userRepository.save(user);
    }

    private void createProfile(
            User user,
            Team team,
            int leaveDays,
            String jobTitle,
            ExperienceLevel level,
            BigDecimal salary
    ) {
        EmployeeProfile profile = new EmployeeProfile();
        profile.setUser(user);
        profile.setTeam(team);
        profile.setRemainingLeaveDays(leaveDays);
        profile.setJobTitle(jobTitle);
        profile.setPhone("+2160000000" + user.getId());
        profile.setAddress("Tunis");
        profile.setSpeciality(team.getSpeciality());
        profile.setHireDate(LocalDate.of(2023, 6, 1));
        profile.setExperienceLevel(level);
        profile.setSalary(salary);
        employeeProfileRepository.save(profile);
    }
}
