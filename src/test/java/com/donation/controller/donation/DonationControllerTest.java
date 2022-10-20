package com.donation.controller.donation;

import com.donation.common.request.donation.DonationSaveReqDto;
import com.donation.config.ConstConfig;
import com.donation.domain.embed.Write;
import com.donation.domain.entites.Post;
import com.donation.domain.entites.User;
import com.donation.domain.enums.PostState;
import com.donation.domain.enums.Role;
import com.donation.repository.donation.DonationRepository;
import com.donation.repository.post.PostRepository;
import com.donation.repository.user.UserRepository;
import com.donation.service.donation.DonationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static com.donation.domain.enums.Category.ETC;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DonationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DonationService donationService;
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clear() {
        donationRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

    }

    User getUser() {
        User user = User.builder()
                .username("username@naver.com")
                .name("장원진")
                .password("1234")
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    Post getPost() {
        Post post = Post.builder()
                .user(getUser())
                .write(new Write("title", "content"))
                .amount(10)
                .category(ETC)
                .state(PostState.WAITING)
                .build();
        return postRepository.save(post);
    }

    private User getSponsor() {
        User sponsor = User.builder()
                .username("username1@naver.com")
                .name("장원진1")
                .password("12341")
                .role(Role.USER)
                .build();
        return userRepository.save(sponsor);
    }

    @Test
    @DisplayName("후원(컨트롤러) : 후원 하기")
    void save() throws Exception {
        //given
        Post post = getPost();
        User sponsor = getSponsor();
        DonationSaveReqDto data = new DonationSaveReqDto(sponsor.getId(), post.getId(), 10.1f);
        String request = objectMapper.writeValueAsString(data);

        //expected
        mockMvc.perform(post("/api/donation")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(print());
    }

    @Test
    @DisplayName("후원(컨트롤러) : 후원 하기_예외발생")
    void saveError() throws Exception {
        //given
        User sponsor = getSponsor();
        DonationSaveReqDto data = new DonationSaveReqDto(sponsor.getId(),null,10.1f);
        String request = objectMapper.writeValueAsString(data);

        //expected
        mockMvc.perform(post("/api/donation")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value("false"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.errorCode").value("400"))
                .andDo(print());
    }


}