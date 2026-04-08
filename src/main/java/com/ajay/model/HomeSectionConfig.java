package com.ajay.model;

import com.ajay.domains.HomeSectionKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "home_section_config", uniqueConstraints = {
        @UniqueConstraint(name = "uc_section_key", columnNames = {"sectionKey"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeSectionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private HomeSectionKey sectionKey;

    private String sectionTitle;

    private boolean visible = true;

    private Integer displayOrder = 0;
}

