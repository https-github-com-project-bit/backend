package com.donation.repository.donation;

import com.donation.common.DonationFixtures;
import com.donation.common.PostFixtures;
import com.donation.common.UserFixtures;
import com.donation.common.utils.RepositoryTest;
import com.donation.domain.entites.Donation;
import com.donation.domain.entites.Post;
import com.donation.domain.entites.User;
import com.donation.repository.post.PostRepository;
import com.donation.repository.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DonationRepositoryTest extends RepositoryTest {
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;
    @Test
    @DisplayName("유저_아이디로_후원내역을_조회한다.")
    void 유저_아이디로_후원내역을_조회한다(){
        //given
        User sponsor = UserFixtures.createUser();
        User beneficiary = UserFixtures.createUser("beneficiary@naver.com");
        userRepository.save(sponsor);
        userRepository.save(beneficiary);

        Post post = PostFixtures.createPost(beneficiary);
        postRepository.save(post);

        Donation donation = DonationFixtures.createDonation(sponsor, post, "10.1");
        donationRepository.save(donation);

        //when
        List<Donation> allByUserId = donationRepository.findAllByUserId(sponsor.getId());

        //then
        Assertions.assertThat(allByUserId.get(0).getId()).isEqualTo(donation.getId());
    }


}
