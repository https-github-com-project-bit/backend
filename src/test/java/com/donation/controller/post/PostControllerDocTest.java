package com.donation.controller.post;

import com.donation.common.request.post.PostSaveReqDto;
import com.donation.config.ConstConfig;
import com.donation.domain.embed.Write;
import com.donation.domain.entites.Post;
import com.donation.domain.entites.User;
import com.donation.domain.enums.Role;
import com.donation.repository.post.PostRepository;
import com.donation.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.donation.domain.enums.Category.ETC;
import static com.donation.domain.enums.PostState.WAITING;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.springdocs.com", uriPort = 443)
@ExtendWith(RestDocumentationExtension.class)
public class PostControllerDocTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConstConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clear(){
        postRepository.deleteAll();
    }

    User getUser() {
        String username = "username@naver.com";
        String name = "정우진";
        String password = "1234";
        Role role = Role.USER;

        return User.builder()
                .username(username)
                .name(name)
                .password(password)
                .role(role)
                .profileImage(config.getBasicImageProfile())
                .build();
    }

    Post getPost() {
        User user = userRepository.save(getUser());
        return Post.builder()
                .write(new Write("title", "content"))
                .user(user)
                .state(WAITING)
                .category(ETC)
                .amount(1)
                .build();
    }

    @Test
    @DisplayName("포스트(RestDocs) : 생성")
    void get() throws Exception {
        //given
        User user = userRepository.save(getUser());
        PostSaveReqDto request = PostSaveReqDto.builder()
                .title("title")
                .content("content")
                .amount(1)
                .category(ETC)
                .build();

        // expected
        mockMvc.perform(post("/api/post?id="+user.getId())
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data.userRespDto.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.userRespDto.name").value(user.getName()))
                .andExpect(jsonPath("$.data.userRespDto.profileImage").value(user.getProfileImage()))
                .andExpect(jsonPath("$.data.write.title").value(request.getTitle()))
                .andExpect(jsonPath("$.data.write.content").value(request.getContent()))
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(document("post-save",
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("id").description("유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.postId").description("포스팅 ID"),
                                fieldWithPath("data.userRespDto.id").description("유저 아이디"),
                                fieldWithPath("data.userRespDto.username").description("이메일"),
                                fieldWithPath("data.userRespDto.name").description("이름"),
                                fieldWithPath("data.userRespDto.profileImage").description("회원 프로필 이미지"),
                                fieldWithPath("data.write.title").description("제목"),
                                fieldWithPath("data.write.content").description("제목"),
                                fieldWithPath("data.amount").description("금액"),
                                fieldWithPath("data.category").description("카테로리"),
                                fieldWithPath("data.state").description("포스팅 상태"),
                                fieldWithPath("data.postDetailImages").description("이미지"),
                                fieldWithPath("error").description("에러 발생시 오류 반환")
                        )

                ));
    }
}