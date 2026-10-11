package com.devboard.repository;

import com.devboard.entity.Project;
import com.devboard.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByOwnerId_returnsOnlyOwnersProjects() {
        User owner1 = createUser("one@example.com", "One");
        User owner2 = createUser("two@example.com", "Two");

        createProject("Project A", owner1);
        createProject("Project B", owner1);
        createProject("Project C", owner2);

        List<Project> owner1Projects = projectRepository.findAllByOwnerIdOrderByCreatedAtDesc(owner1.getId());

        assertThat(owner1Projects).hasSize(2);
        assertThat(owner1Projects).extracting(Project::getName)
                .containsExactlyInAnyOrder("Project A", "Project B");
    }

    @Test
    void findByIdAndOwnerId_returnsEmptyWhenNotOwned() {
        User owner = createUser("owner@example.com", "Owner");
        User other = createUser("other@example.com", "Other");
        Project project = createProject("Mine", owner);

        Optional<Project> found = projectRepository.findByIdAndOwnerId(project.getId(), other.getId());

        assertThat(found).isEmpty();
    }

    private User createUser(String email, String name) {
        User u = new User();
        u.setEmail(email);
        u.setPassword("hashed");
        u.setDisplayName(name);
        return userRepository.save(u);
    }

    private Project createProject(String name, User owner) {
        Project p = new Project();
        p.setName(name);
        p.setDescription("desc");
        p.setOwner(owner);
        return projectRepository.save(p);
    }
}