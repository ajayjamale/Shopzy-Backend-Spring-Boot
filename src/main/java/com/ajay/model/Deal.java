package com.ajay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String subtitle;
    private String description;
    private String image;
    private String discountLabel;
    private String redirectLink;

    private Integer discount;
    private Boolean active = true;
    private Boolean featured = true;
    private Integer displayOrder = 0;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    private HomeCategory category;

}
