package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.Comment;
import greensnail_backend.GreenSnail.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    int countByPost(Post post);
}
