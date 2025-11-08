package com.example.groupmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Voting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"group", "votingResults"})
public class Voting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voteId")
    private Integer voteId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId", nullable = false)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Group group;
    
    @Column(name = "topic", nullable = false, length = 255)
    private String topic;
    
    @Column(name = "optionA", length = 100)
    private String optionA;
    
    @Column(name = "optionB", length = 100)
    private String optionB;
    
    @Column(name = "finalResult", length = 100)
    private String finalResult;
    
    @Column(name = "totalVotes")
    private Integer totalVotes = 0;
    
    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "voting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private List<VotingResult> votingResults;
}
