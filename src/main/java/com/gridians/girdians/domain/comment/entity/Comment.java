package com.gridians.girdians.domain.comment.entity;

import com.gridians.girdians.domain.comment.dto.CommentDto;
import com.gridians.girdians.domain.user.entity.User;
import com.gridians.girdians.global.entity.BaseEntity;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Comment extends BaseEntity implements Serializable {

    @Id @GeneratedValue
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Column(name = "content")
    private String content;

    public static Comment from(CommentDto.Request request){
        return Comment.builder()
                .content(request.getContents())
                .user(request.getUser())
                .build();
    }

    public void update (CommentDto.Request request){
        this.content = request.getContents();
        this.user = request.getUser();
    }
}