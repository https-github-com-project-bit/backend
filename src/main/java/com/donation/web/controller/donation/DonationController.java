package com.donation.web.controller.donation;

import com.donation.common.CommonResponse;
import com.donation.common.request.donation.DonationSaveReqDto;
import com.donation.service.donation.DonationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/donation")
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    public ResponseEntity<?> donation(
            @RequestBody @Valid DonationSaveReqDto donationSaveReqDto
    ) {
        donationService.save(donationSaveReqDto);
        return new ResponseEntity<>(CommonResponse.success(), HttpStatus.CREATED);
    }


}