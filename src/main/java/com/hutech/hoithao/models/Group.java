package com.hutech.hoithao.models;
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
    @Column
    private Integer id_sport; ;
    @OneToMany(mappedBy = "group")
    private List<Team> listTeam;
}
