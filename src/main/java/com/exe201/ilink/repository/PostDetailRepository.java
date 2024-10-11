package com.exe201.ilink.repository;

import com.exe201.ilink.model.entity.PostDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostDetailRepository extends JpaRepository<PostDetail, Long> {

    Optional<List<PostDetail>> findByPostId(Long id);
}
