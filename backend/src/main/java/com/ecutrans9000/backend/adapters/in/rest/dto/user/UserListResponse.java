package com.ecutrans9000.backend.adapters.in.rest.dto.user;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
  private List<UserResponse> content;
  private Integer page;
  private Integer size;
  private Long totalElements;
  private Integer totalPages;
}
