package com.lmserver.entity.gg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideosId implements Serializable {
    private String id;
    private Long ownerId;
}