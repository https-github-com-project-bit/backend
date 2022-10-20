package com.donation.service.favorite;

import com.donation.common.response.user.UserRespDto;
import com.donation.domain.entites.Favorites;
import com.donation.repository.favorite.FavoriteRedisRepository;
import com.donation.repository.favorite.FavoriteRepository;
import com.donation.service.post.PostService;
import com.donation.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final FavoriteRedisRepository favoriteRedisRepository;
    private final UserService userService;
    private final PostService postService;

    public void saveAndCancel(Long postId, Long userId){
        if(findById(postId, userId)){
            cancel(postId, userId);
            return;
        }
        favoriteRedisRepository.save(postId, userId);
        favoriteRepository.save(Favorites.of(userService.getUser(userId), postService.getPost(postId)));
    }

    private void cancel(Long postId, Long userId){
        favoriteRedisRepository.delete(postId, userId);
        favoriteRepository.deleteByPost_IdAndUser_Id(postId,userId);
    }


    public List<UserRespDto> findAll(Long postId){
        List<Long> userId = favoriteRedisRepository.findAll(postId);
        return userService.getListIdIn(userId).stream()
                .map(UserRespDto::new)
                .collect(Collectors.toList());
    }

    public Long count(Long postId){
        return favoriteRedisRepository.count(postId);
    }

    public void deletePostId(Long postId){
        favoriteRedisRepository.deleteAll(postId);
        favoriteRepository.deleteAllByPost_Id(postId);
    }

    public Boolean findById(Long postId, Long userId){
        return favoriteRedisRepository.findById(postId, userId);
    }
}