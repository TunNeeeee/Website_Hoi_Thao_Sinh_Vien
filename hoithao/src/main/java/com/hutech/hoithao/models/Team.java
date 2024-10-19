package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "Team")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // Ten cua team thi dau,
    // Neu la noi dung don thi Ten Team la Ten Tuyen Thu
    // Neu la noi dung doi thi Ten Team la Ten Tuyen Thu 1/ Ten Tuyen Thu 2
    @Column(name = "team_name")
    private String teamName;
    // Dùng để lưu hình ảnh minh chứng
    @Lob
    @Column(name = "payment_proof", columnDefinition = "BLOB")
    private byte[] paymentProof;
    // Trang thai cua team/ tuyen thu
    // -1: Khong duyet don , 0: Dang duyen don , 1: Da duyet
    //  2: Bi loai , 3: Bo giai , 10: Vo dich
    @Column(name = "status")
    private Integer status;
    // So game dau vong bang, neu knock out thi = 0
    @Column(name = "number_game")
    private Integer numberGame;
    // So diem ghi duoc o vong bang
    @Column(name = "point")
    private Integer point;
    // Hieu so vong bang
    @Column(name = "hs")
    private Integer hs;
    // Xep hang vong bang
    @Column(name = "no_rank")
    private Integer noRank;
    // Xep hang chung cuoc - 1: Vo dich 2: A quan 3: Hang 3
    @Column
    private int noFinal;
    public Team(String name, byte[] paymentProof) {
        this.teamName = name;
        this.paymentProof = paymentProof;
    }

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
    @ManyToOne
    @JoinColumn(name = "id_sport")
    private Sport sport;
    @OneToMany(mappedBy = "team")
    private Set<Member> listMember;
//    @OneToMany(mappedBy = "team1")
//    private Set<Match> homeMatches;
//
//    @OneToMany(mappedBy = "team2")
//    private Set<Match> awayMatches;
//    @OneToMany(mappedBy = "team")
//    private Set<RuleDetail> listRuleDetail;
//    @ManyToOne
//    @JoinColumn(name = "id_rank")
//    private Rank rank;

}
