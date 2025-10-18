package com.example.taskify.web;

import com.example.taskify.domain.comment.Comment;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  CommentResponse toResponse(Comment comment);

  List<CommentResponse> toResponses(List<Comment> comments);
}
