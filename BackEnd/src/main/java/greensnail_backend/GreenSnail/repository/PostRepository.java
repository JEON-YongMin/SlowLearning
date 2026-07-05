package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.AgeCategory;
import greensnail_backend.GreenSnail.entity.JobCategory;
import greensnail_backend.GreenSnail.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.jobCategory = :jobCategory")
    List<Post> findByJobCategory(@Param("jobCategory") JobCategory jobCategory);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.ageCategory = :ageCategory")
    List<Post> findByAgeCategory(@Param("ageCategory") AgeCategory ageCategory);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.jobCategory = :jobCategory AND p.ageCategory = :ageCategory")
    List<Post> findByJobCategoryAndAgeCategory(@Param("jobCategory") JobCategory jobCategory,
                                               @Param("ageCategory") AgeCategory ageCategory);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.title LIKE %:keyword%")
    List<Post> findByTitleContaining(@Param("keyword") String keyword);

    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    List<Post> findAllWithUserOrderByCreatedAtDesc();

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.providerId = :providerId ORDER BY p.createdAt DESC")
    List<Post> findByUser_ProviderIdOrderByCreatedAtDesc(@Param("providerId") String providerId);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.comments " +
            "ORDER BY p.createdAt DESC")
    List<Post> findAllWithUserAndCommentsOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.comments " +
            "WHERE p.jobCategory = :jobCategory AND p.ageCategory = :ageCategory")
    List<Post> findByJobCategoryAndAgeCategoryWithComments(@Param("jobCategory") JobCategory jobCategory,
                                                           @Param("ageCategory") AgeCategory ageCategory);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.comments " +
            "WHERE p.title LIKE %:keyword%")
    List<Post> findByTitleContainingWithComments(@Param("keyword") String keyword);
}
