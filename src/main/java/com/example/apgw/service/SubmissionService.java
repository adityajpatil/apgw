package com.example.apgw.service;

import com.example.apgw.model.*;
import com.example.apgw.repository.AssignmentRepository;
import com.example.apgw.repository.StudentRepository;
import com.example.apgw.repository.StudentSubjectRepository;
import com.example.apgw.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class SubmissionService {

    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final UserService userService;
    private final StudentSubjectRepository studentSubjectRepository;
    private final SubmissionRepository submissionRepository;
    @Value("${file-path}")
    String basedir;

    @Autowired
    public SubmissionService(AssignmentRepository assignmentRepository,
                             StudentRepository studentRepository,
                             UserService userService,
                             StudentSubjectRepository studentSubjectRepository,
                             SubmissionRepository submissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
        this.userService = userService;
        this.studentSubjectRepository = studentSubjectRepository;
        this.submissionRepository = submissionRepository;
    }

    public String addSubmission(Long AssignmentId, MultipartFile file) {
        //find Assignment
        Assignment assignment = assignmentRepository.findOne(AssignmentId);

        //check student is allowed to upload
        Subject assignmentSubject = assignment.getSubject();
        Long subjectId = assignmentSubject.getId();
        Student student = studentRepository.findOne(userService.getEmail());
        String email = student.getEmail();

        StudentSubjectId idObject = new StudentSubjectId(subjectId, email);
        StudentSubject studentSubject = studentSubjectRepository.findOne(idObject);
        if (studentSubject == null) {
            return "Permission denied";
        }

        //Create temp submission
        Submission submissionTemp = new Submission(assignment, student, "");
        Submission submission = submissionRepository.save(submissionTemp);

        //set path
        String path = basedir + "/apgw/submission/" + submission.getId() + "/";
        if (!new File(path).exists()) {
            boolean mkdir = new File(path).mkdirs();
            if (!mkdir) {
                return "Error creating dir";
            }
        }

        //Save files
        String submissionPath = path + file.getOriginalFilename();
        File dest = new File(submissionPath);

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            //if failed, update db
            e.printStackTrace();
            submissionRepository.delete(submission.getId());
            return "FS error";
        }

        //update db
        submission.setUploadPath(submissionPath);
        submissionRepository.save(submission);
        return "created";
    }

    public List<Submission> getSubmissions(Long assignmentId) {
        Assignment assignment = assignmentRepository.findOne(assignmentId);
        return assignment.getSubmissions();
    }
}