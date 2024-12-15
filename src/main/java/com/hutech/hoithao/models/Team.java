package com.hutech.hoithao.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Team")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Tên của team thi đấu
    @Column(name = "team_name", nullable = false, length = 255)
    private String teamName;

    // Dùng để lưu hình ảnh minh chứng
    @Column(name = "payment_proof_path", length = 500, nullable = true)
    private String paymentProofPath;

    // Trạng thái của team/tuyển thủ
    // -1: Không duyệt đơn, 0: Đang duyệt đơn, 1: Đã duyệt
    //  2: Bị loại, 3: Bỏ giải, 10: Vô địch
    @Column(name = "status", nullable = false)
    private Integer status;

    // Số game đấu vòng bảng, nếu knock-out thì = 0
    @Column(name = "number_game")
    private Integer numberGame;

    // Số điểm ghi được ở vòng bảng
    @Column(name = "point")
    private Integer point;

    // Hiệu số vòng bảng
    @Column(name = "hs")
    private Integer hs;

    // Xếp hạng vòng bảng
    @Column(name = "no_rank")
    private Integer noRank;

    // Xếp hạng chung cuộc (1: Vô địch, 2: Á quân, 3: Hạng 3)
    @Column(name = "no_final")
    private Integer noFinal;

    // Constructor dùng khi chỉ tạo team với tên và hình ảnh
    public Team(String name, String paymentProofPath) {
        this.teamName = name;
        this.paymentProofPath = paymentProofPath;
    }

    // Quan hệ với bảng User (nhiều team có thể thuộc một user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    // Quan hệ với bảng Sport (nhiều team thuộc một môn thể thao)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sport")
    private Sport sport;

    // Danh sách thành viên trong team
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Member> listMember = new HashSet<>();

    // Các trận đấu mà team là đội nhà
    @OneToMany(mappedBy = "team1", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Match> homeMatches = new HashSet<>();

    // Các trận đấu mà team là đội khách
    @OneToMany(mappedBy = "team2", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Match> awayMatches = new HashSet<>();

    // Danh sách chi tiết luật áp dụng cho team
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RuleDetail> listRuleDetail = new HashSet<>();

    // Nhóm mà team thuộc về
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_group")
    private Group group;

}
