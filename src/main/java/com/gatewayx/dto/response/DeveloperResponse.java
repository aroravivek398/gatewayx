package com.gatewayx.dto.response;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeveloperResponse {

    private Long id;
    private String email;


    private String name;

    private LocalDateTime createdAt;



}
