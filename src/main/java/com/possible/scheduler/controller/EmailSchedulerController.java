package com.possible.scheduler.controller;

import com.possible.scheduler.payload.EmailRequest;
import com.possible.scheduler.payload.EmailResponse;
import com.possible.scheduler.processor.EmailJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.xml.crypto.Data;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.util.Date.from;

@Slf4j
@RestController
@RequestMapping("/schedule")
public class EmailSchedulerController {
    @Autowired
    private Scheduler scheduler;

    @PostMapping("/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest request){
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(request.getDateTime(), request.getTimeZone());
            if (dateTime.isBefore(ZonedDateTime.now())){
                EmailResponse res = new EmailResponse(false, "Invalid parameter");
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            JobDetail jobDetail = buildJobDetail(request);
            Trigger trigger = buildTrigger(jobDetail, dateTime);
            scheduler.scheduleJob(jobDetail, trigger);
            EmailResponse res = new EmailResponse(false,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(),
                    "Email scheduled successfully");
            return new ResponseEntity<>(res, HttpStatus.OK);

        }catch (SchedulerException se){
            log.error("Error while scheduling email: ", se);
            EmailResponse res = new EmailResponse(false, "Error while scheduling email, please try again later");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private JobDetail buildJobDetail(EmailRequest scheduleEmailReq){
        JobDataMap dataMap = new JobDataMap();

        dataMap.put("email", scheduleEmailReq.getEmail());
        dataMap.put("subject", scheduleEmailReq.getSubject());
        dataMap.put("body", scheduleEmailReq.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send email job")
                .usingJobData(dataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "Email-trigger")
                .withDescription("Send email trigger")
                .startAt(from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
