package com.lmserver.entity.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tags")
public class Tags {

    @Id
    private String key;

    private String value;

}