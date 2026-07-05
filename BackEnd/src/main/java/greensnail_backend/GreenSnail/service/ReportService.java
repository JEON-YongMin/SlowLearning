package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.entity.*;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public void reportPost(Long postId, String providerId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User reporter = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(post.getUser())
                .post(post)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        reportRepository.save(report);

        checkAndBlockUser(post.getUser());
    }

    public void reportComment(Long commentId, String providerId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        User reporter = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(comment.getPost().getUser())
                .comment(comment)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        reportRepository.save(report);

        checkAndBlockUser(comment.getPost().getUser());
    }

    public void reportUser(Long reportedUserId, String providerId, String reason) {
        User reportedUser = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User reporter = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        reportRepository.save(report);

        checkAndBlockUser(reportedUser);
    }

    private void checkAndBlockUser(User user) {
        int reportCount = reportRepository.countByReportedUser(user);
        if (reportCount >= 3) {
            user.setBlocked(true);
            userRepository.save(user);
        }
    }
}