package com.hutech.hoithao.models;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "`group`")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    //Ten bang dau
    @Column
    private String groupName;
    //ID Sport
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sport", nullable = false)
    private Sport sport; // Thêm quan hệ với Sport
    @OneToMany(mappedBy = "group")
    @JsonManagedReference
    private List<Team> listTeam;

}
