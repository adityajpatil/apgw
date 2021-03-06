package com.example.apgw.service;

import com.example.apgw.helper.FileStorageHelper;
import com.example.apgw.helper.GradingHelper;
import com.example.apgw.model.*;
import com.example.apgw.repository.AssignmentRepository;
import com.example.apgw.repository.StudentRepository;
import com.example.apgw.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.acl.NotOwnerException;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final UserService userService;
    private final StudentRepository studentRepository;
    @Value("${file-path}")
    String basedir;

    /**
     * Constructor for Assignment Service.
     *
     * @param assignmentRepository Repository for assignment.
     * @param teacherRepository    Repository for teacher.
     * @param userService          Repository for userService.
     * @param studentRepository    Repository for Student.
     */
    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository,
                             TeacherRepository teacherRepository,
                             UserService userService,
                             StudentRepository studentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.teacherRepository = teacherRepository;
        this.userService = userService;
        this.studentRepository = studentRepository;
    }

    /**
     * Add Assignment to the subject.
     *
     * @param subjectName  Name of subject to which assignment is to be added.
     * @param title        Title fo assignment.
     * @param inputFile    File containing input test cases.
     * @param outputFile   File containing output test cases.
     * @param questionFile File containing question details.
     */
    public void addAssignment(String subjectName,
                              String title,
                              MultipartFile inputFile,
                              MultipartFile outputFile,
                              MultipartFile questionFile) throws Exception {

        // find relevant subject
        String teacherEmail = userService.getEmail();
        Subject subject = getSubject(subjectName, teacherEmail);

        // check title and files are not empty
        if (title.isEmpty() || inputFile.isEmpty()
                || outputFile.isEmpty() || questionFile.isEmpty()) {
            throw new Exception("empty input");
        }

        // create Assignment
        Assignment assignment = assignmentRepository.save(new Assignment(subject, title));
        Long id = assignment.getId();

        //try to save files
        try {
            new FileStorageHelper(basedir)
                    .storeAssignmentFiles(id, inputFile, outputFile, questionFile);
        } catch (IOException ex) {
            //delete assignment
            assignmentRepository.delete(id);
            throw ex;
        }
    }

    /**
     * Get subject of a teacher.
     *
     * @param subjectName  name of subject.
     * @param teacherEmail email id of teacher.
     * @return Subject.
     * @throws NotOwnerException if subject name is invalid,
     *                           or teacher does not own the subject.
     */
    private Subject getSubject(String subjectName, String teacherEmail) throws NotOwnerException {
        Teacher teacher = teacherRepository.findOne(teacherEmail);
        return teacher.getSubjects().stream()
                .filter(s -> s.getDetails().getName().equals(subjectName))
                .findFirst()
                .orElseThrow(NotOwnerException::new);
    }

    /**
     * List all assignments, search by subjects.
     *
     * @param subjectName name of subject.
     * @return List of assignments.
     */
    public List<Assignment> getAssignments(String subjectName) throws NotOwnerException {
        //get subject
        String teacherEmail = userService.getEmail();
        Subject subject = getSubject(subjectName, teacherEmail);
        return subject.getAssignments();
    }

    /**
     * List all assignments, search by Id.
     *
     * @param id Id of subject.
     * @return List of assignment.
     */
    public List<Assignment> getAssignmentsById(Long id) throws NotOwnerException {
        String email = userService.getEmail();
        Student student = studentRepository.findOne(email);
        StudentSubject studentSubject = student.getSubjects().stream()
                .filter(studentSubject1 ->
                        studentSubject1.getSubject().getId().equals(id))
                .findFirst()
                .orElseThrow(NotOwnerException::new);
        return studentSubject.getSubject().getAssignments();
    }

    /**
     * Grade all submissions for an assignment.
     *
     * @param id id of assignment.
     * @throws NotOwnerException    If current user is not the owner of the subject.
     */
    public void grade(Long id) throws NotOwnerException {
        //get the Assignment
        Assignment assignment = assignmentRepository.findOne(id);

        //match with Teacher
        String teacherEmail = userService.getEmail();
        if (!assignment.getSubject().getTeacher().getEmail().equals(teacherEmail)) {
            throw new NotOwnerException();
        }

        //get all submissions
        List<Submission> submissions = assignment.getSubmissions();
        Path path = Paths.get(basedir + "/apgw/temp/");

        //grade every submission
        submissions.forEach(submission ->
        {
            try {
                new GradingHelper(basedir).testSubmission(submission, assignment, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        assignment.setSubmissions(submissions);
        assignmentRepository.save(assignment);
    }

    /**
     * Get an assignment.
     *
     * @param id id of assignment to retrieve.
     * @return Assignment.
     */
    public Assignment getAssignment(Long id) {
        return assignmentRepository.findOne(id);
    }

    /**
     * get list of submissions.
     *
     * @param assignmentId get id of assignment.
     * @return list of submission.
     */
    public List<Submission> getSubmissions(Long assignmentId) {
        Assignment assignment = assignmentRepository.findOne(assignmentId);
        return assignment.getSubmissions();
    }

    /**
     * get path of question file.
     *
     * @param id id of assignment.
     * @return path of question file.
     */
    public Path getQuestionPath(Long id) {
        return Paths.get(basedir + "/apgw/assi/" + id + "/question");
    }


    /**
     * Delete an assignment.
     * @param id id of assignment to be deleted.
     */
    public void deleteAssignment(Long id) {
        assignmentRepository.delete(id);
    }
}
