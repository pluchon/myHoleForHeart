package org.example.treehole.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserNutrition {
    private Long userId;
    private Integer seedCount;
    private LocalDate lastCheckInDate;
    private Integer continuousDays;
    private Integer totalDays;
}
