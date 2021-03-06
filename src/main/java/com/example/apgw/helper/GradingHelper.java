package com.example.apgw.helper;

import com.example.apgw.model.Assignment;
import com.example.apgw.model.Submission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class GradingHelper {

    private String basedir;

    public GradingHelper(String basedir) {
        this.basedir = basedir;
    }

    /**
     * Tests the submitted code against test cases.
     *
     * @param submission submission to be tested.
     * @param assignment assignment related to submission.
     * @param tempPath   Path of temp dir.
     * @throws IOException          If parsing result fails.
     * @throws InterruptedException If process fails.
     * @throws URISyntaxException   If file copying fails.
     */
    public void testSubmission(Submission submission,
                               Assignment assignment,
                               Path tempPath)
            throws IOException, InterruptedException, URISyntaxException {
        FileStorageHelper fileStorageHelper = new FileStorageHelper(basedir);
        FileCopyHelper fileCopyHelper = new FileCopyHelper(basedir);

        //copy files
        try {
            fileCopyHelper.copyFiles(submission, assignment, tempPath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw e;
        }

        //run docker
        String line = runDocker(submission, tempPath, fileStorageHelper, fileCopyHelper);

        //calculate marks
        int marks = 0;
        try {
            marks = Integer.parseInt(line);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //update marks
        submission.setMarks(marks);

        //delete files from temp
        fileStorageHelper.deleteTemp(tempPath);
    }

    /**
     * Run docker containers to grade assignments.
     *
     * @param submission        Submission to grade.
     * @param tempPath          Path where temporary files are stored.
     * @param fileStorageHelper Instance of fileStorageHelper class.
     * @param fileCopyHelper    Instance of fileCopyHelper class.
     * @return marks scored.
     * @throws IOException          If I/O fails.
     * @throws InterruptedException If process is interrupted.
     */
    private String runDocker(Submission submission,
                             Path tempPath,
                             FileStorageHelper fileStorageHelper,
                             FileCopyHelper fileCopyHelper)
            throws IOException, InterruptedException {
        String dockerCommand;
        String type = fileCopyHelper.getCodeType(submission);
        switch (type) {
            case "c":
                dockerCommand = "docker run --rm -v "
                        + tempPath + "/:/home/files/ -w /home/files gcc:7.3 ./c-script.sh";
                break;
            case "cpp":
                dockerCommand = "docker run -e CodeFileExt="
                        + fileStorageHelper.getExtension(submission)
                        + " --rm -v "
                        + tempPath + "/:/home/files/ -w /home/files gcc:7.3 ./cpp-script.sh";
                break;
            default:
                dockerCommand = "";
        }
        Process process = Runtime.getRuntime().exec(dockerCommand);
        process.waitFor();
        InputStreamReader isReader = new InputStreamReader(process.getInputStream());
        return new BufferedReader(isReader).readLine();
    }
}
