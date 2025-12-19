package org.nsu.animal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nsu.files.entity.File;

@Entity
@Table(name = "t_animals_cards_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalCardFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_animal_card", nullable = false)
    private AnimalCard animalCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_file", nullable = false)
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type")
    private AnimalCardFileType fileType;
}
