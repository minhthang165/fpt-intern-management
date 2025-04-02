package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.ClassType;
import com.fsoft.fintern.enums.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingDTO {
    private String classId;
    private String className;
    private ClassType classType;
    private LanguageType languageType;
    private Integer roomId;
} 