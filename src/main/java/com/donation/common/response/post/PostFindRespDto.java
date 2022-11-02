package com.donation.common.response.post;

import com.donation.common.response.user.UserRespDto;
import com.donation.domain.embed.Write;
import com.donation.domain.entites.Post;
import com.donation.domain.entites.PostDetailImage;
import com.donation.domain.enums.Category;
import com.donation.domain.enums.PostState;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class PostFindRespDto {
    private Long postId;

    private UserRespDto userRespDto;

    private Write write;

    private String amount;
    private float currentAmount;

    private Category category;

    private PostState state;

    private List<String> postDetailImages;

    private int favoriteCount;

    @Builder
    public PostFindRespDto(final Long postId, final UserRespDto userRespDto,final Write write,final String amount,final float currentAmount,
                           final Category category, final PostState state,final List<String> postDetailImages,final int favoriteCount) {
        this.postId = postId;
        this.userRespDto = userRespDto;
        this.write = write;
        this.amount = amount;
        this.currentAmount = currentAmount;
        this.category = category;
        this.state = state;
        this.postDetailImages = postDetailImages;
        this.favoriteCount = favoriteCount;
    }

    public static PostFindRespDto of(Post post){
        return PostFindRespDto.builder()
                .postId(post.getId())
                .userRespDto(UserRespDto.of(post.getUser()))
                .write(post.getWrite())
                .amount(post.getAmount())
                .currentAmount(post.getCurrentAmount())
                .category(post.getCategory())
                .state(post.getState())
                .postDetailImages(getPostDetailImages(post))
                .favoriteCount(post.getFavorites().size())
                .build();
    }

    public static List<String> getPostDetailImages(Post post) {
        return post.getPostDetailImages().stream()
                .map(PostDetailImage::getImagePath)
                .collect(Collectors.toList());
    }
}
