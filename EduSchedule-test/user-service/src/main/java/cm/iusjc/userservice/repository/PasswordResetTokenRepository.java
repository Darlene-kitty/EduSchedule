package cm.iusjc.userservice.repository;

import cm.iusjc.userservice.entity.PasswordResetToken;
import cm.iusjc.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    void deleteByExpiryDateBefore(LocalDateTime now);
    
    List<PasswordResetToken> findByUser(User user);
    
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user = :user AND p.used = false")
    void invalidateAllTokensForUser(@Param("user") User user);
    
    @Query("SELECT p FROM PasswordResetToken p WHERE p.user = :user AND p.used = false AND p.expiryDate > :now")
    List<PasswordResetToken> findActiveTokensForUser(@Param("user") User user, @Param("now") LocalDateTime now);
}
