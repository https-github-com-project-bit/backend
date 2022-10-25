package com.donation.controller.user;


import com.donation.common.request.user.UserJoinReqDto;
import com.donation.common.request.user.UserLoginReqDto;
import com.donation.domain.entites.User;
import com.donation.repository.user.UserRepository;
import com.donation.service.s3.AwsS3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.donation.testutil.TestDtoDataFactory.createUserJoinReqDto;
import static com.donation.testutil.TestDtoDataFactory.createUserLoginReqDto;
import static com.donation.testutil.TestEntityDataFactory.createUser;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.springdocs.com", uriPort = 443)
@ExtendWith(RestDocumentationExtension.class)
public class UserControllerDocTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private AwsS3Service s3Service;

    @AfterEach
    void clear(){
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("회원(RestDocs) : 회원 가입")
    void join() throws Exception {
        //given
        UserJoinReqDto request = createUserJoinReqDto();

        // expected
        mockMvc.perform(post("/api/join")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(document("user-join",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data").description("데이터"),
                                fieldWithPath("error").description("에러 발생시 오류 반환")
                        )
                ));
    }

    @Test
    @DisplayName("회원(RestDocs) : 로그인")
    void login() throws Exception {
        //given
        User user = userRepository.save(createUser());
        UserLoginReqDto request = createUserLoginReqDto(user.getUsername(), user.getPassword());

        // expected
        mockMvc.perform(post("/api/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data").value(user.getId()))
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(document("user-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data").description("유저 ID"),
                                fieldWithPath("error").description("에러 발생시 오류 반환")
                        )
                ));
    }

    @Test
    @DisplayName("회원(RestDocs) : 단건 조회")
    void get() throws Exception {
        //given
        User user = userRepository.save(createUser());

        // expected
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/user/{id}", user.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.name").value(user.getName()))
                .andExpect(jsonPath("$.data.profileImage").value(user.getProfileImage()))
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(document("user-inquiry",
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("유저 ID"),
                                fieldWithPath("data.username").description("이메일"),
                                fieldWithPath("data.name").description("이름"),
                                fieldWithPath("data.profileImage").description("회원 프로필 이미지"),
                                fieldWithPath("error").description("에러 발생시 오류 반환")
                        )

                ));
    }

    @Test
    @DisplayName("회원(RestDocs) : 회원 리스트 조회")
    void list() throws Exception {
        //given
        List<User> users = IntStream.range(1, 31)
                .mapToObj(i -> createUser("username" + i))
                .collect(Collectors.toList());
        userRepository.saveAll(users);

        // expected
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data.content.length()", Matchers.is(10)))
                .andExpect(jsonPath("$.data.content[0].username").value(users.get(0).getUsername()))
                .andExpect(jsonPath("$.data.content[0].name").value(users.get(0).getName()))
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(document("user-getList",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("회원(컨트롤러) : 회원 삭제")
    void delete() throws Exception {
        //given
        User user = userRepository.save(createUser());

        // expected
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(document("user-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @Transactional
    @DisplayName("회원(정보수정) : 프로필 변경")
    void update_profile() throws Exception {
        //given
        User user = userRepository.save(createUser());

        MultipartFile profile = new MockMultipartFile("test1", "test1.PNG", MediaType.IMAGE_PNG_VALUE, "test1".getBytes());

        // expected
        mockMvc.perform(multipart("/api/user/{postId}/profile", user.getId())
                        .file("profile", profile.getBytes())
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setMethod("PUT");
                            return requestPostProcessor;
                        })
                        .contentType(MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty())
                .andDo(document("user-profileImage",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        em.flush();
        em.clear();

        //S3 파일 삭제
        User find = userRepository.findById(user.getId()).get();
        s3Service.delete(find.getProfileImage());
    }
}
