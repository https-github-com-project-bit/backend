package com.donation.domain.post.entity;

import com.donation.domain.post.dto.PostUpdateReqDto;
import com.donation.infrastructure.embed.Write;
import com.donation.infrastructure.embed.BaseEntity;
import com.donation.domain.favorite.entity.Favorites;
import com.donation.domain.user.entity.User;
import com.donation.global.exception.DonationInvalidateException;
import com.donation.global.exception.DonationNotFoundException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Table(name="post")
@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Post extends BaseEntity {
    private static final float MAX_AMOUNT = 3000f;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "post_id")
    private Long id;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Embedded
    private Write write;
    private String amount;
    private float currentAmount;
    @Enumerated(STRING)
    private Category category;
    @Enumerated(STRING)
    private PostState state;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostDetailImage> postDetailImages = new ArrayList<>();


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<Favorites> favorites = new HashSet<>();

    @Builder
    public Post(Long id, User user, Write write, String amount, Category category, PostState state) {
        this.id = id;
        this.user = user;
        this.write = write;
        this.amount = amount;
        this.currentAmount = 0;
        this.category = category;
        this.state = state;
    }

    public void addPostImage(PostDetailImage postDetailImage){
        postDetailImage.setPost(this);
        this.getPostDetailImages().add(postDetailImage);
    }

    public void addFavorite(Favorites favorites){
        favorites.setPost(this);
        this.getFavorites().add(favorites);
    }

    public Post changePost(PostUpdateReqDto dto) {
        this.write = dto.getWrite();
        this.category = dto.getCategory();
        this.amount = dto.getAmount();
        return this;
    }

    public Post confirm(PostState state) {
        this.state=state;
        return this;
    }

    public void validateOwner(Long useId) {
        if (!useId.equals(user.getId())) {
            throw new DonationInvalidateException("게시물의 작성자만 권한이 있습니다.");
        }
    }

    public void increase(final float amount){
        if(this.currentAmount + amount > MAX_AMOUNT){
            throw new DonationNotFoundException("목표금액보다 금액이 커질 수 없습니다.");
        }
        this.currentAmount = this.currentAmount + amount;
    }
}
