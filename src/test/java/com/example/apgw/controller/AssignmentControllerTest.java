package com.example.apgw.controller;

import com.example.apgw.model.Assignment;
import com.example.apgw.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class AssignmentControllerTest {

    private AssignmentController subject;
    @Mock
    private AssignmentService service;

    @BeforeEach
    void setUp() {
        initMocks(this);
        subject = new AssignmentController(service);
    }

    @Test
    void addAssignmentShouldReturnNotModifiedOnIOException() throws Exception {
        doThrow(new IOException()).when(service).addAssignment(any(), any(), any(), any(), any());
        MockMultipartFile file = new MockMultipartFile("inputFile",
                "input.txt",
                "text/plain",
                "some text".getBytes());
        ResponseEntity<String> reply =
                subject.addAssignment("Foo", "", file, file, file);
        assertEquals(HttpStatus.NOT_MODIFIED, reply.getStatusCode());
    }
    @Test
    void addAssignmentShouldReturnCreatedOnSuccess() throws Exception {
        doNothing().when(service).addAssignment(any(), any(), any(), any(), any());
        MockMultipartFile file = new MockMultipartFile("inputFile",
                "input.txt",
                "text/plain",
                "some text".getBytes());
        ResponseEntity<String> reply =
                subject.addAssignment("Foo", "", file, file, file);

        assertEquals(HttpStatus.CREATED, reply.getStatusCode());
    }

    @Test
    void addAssignmentShouldReturnNoContentOnEmptyTitle() throws Exception {
        doThrow(new Exception()).when(service).addAssignment(any(), any(), any(), any(), any());
        MockMultipartFile file = new MockMultipartFile("inputFile",
                "input.txt",
                "text/plain",
                "some text".getBytes());
        ResponseEntity<String> reply =
                subject.addAssignment("Foo", "", file, file, file);

        assertEquals(HttpStatus.NO_CONTENT, reply.getStatusCode());
    }

    @Test
    void addAssignmentShouldReturnNoContentOnEmptyFile() throws Exception {
        doThrow(new Exception()).when(service).addAssignment(any(), any(), any(), any(), any());
        MockMultipartFile file = new MockMultipartFile("inputFile",
                "input.txt",
                "text/plain",
                "".getBytes());
        ResponseEntity<String> reply =
                subject.addAssignment("Foo", "Bar", file, file, file);

        assertEquals(HttpStatus.NO_CONTENT, reply.getStatusCode());
    }

    @Test
    void getAssignments() throws NotOwnerException {
        List<Assignment> list = new ArrayList<>();
        String name = "Foo";
        given(service.getAssignments(name)).willReturn(list);
        ResponseEntity<List<Assignment>> reply = subject.getAssignments(name);
        assertEquals(HttpStatus.OK, reply.getStatusCode());
    }

    @Test
    void getAssignmentsShouldReturnNotFound() throws NotOwnerException {
        String name = "Foo";
        doThrow(new NotOwnerException()).when(service).getAssignments(name);
        ResponseEntity<List<Assignment>> reply = subject.getAssignments(name);
        assertEquals(HttpStatus.NOT_FOUND, reply.getStatusCode());
    }

    @Test
    void getAssignmentsById() throws NotOwnerException {
        List<Assignment> list = new ArrayList<>();
        long id = 1;
        given(service.getAssignmentsById(id)).willReturn(list);
        ResponseEntity<List<Assignment>> reply = subject.getAssignmentsById(id);
        assertEquals(HttpStatus.OK, reply.getStatusCode());
    }

    @Test
    void getassignmentsByIdShouldReturnNotFound() throws NotOwnerException {
        long id = 1;
        doThrow(new NotOwnerException()).when(service).getAssignmentsById(id);
        ResponseEntity<List<Assignment>> reply = subject.getAssignmentsById(id);
        assertEquals(HttpStatus.NOT_FOUND, reply.getStatusCode());
    }

    @Test
    void gradeShouldReturnCreated() throws NotOwnerException {
        long id = 1;
        doNothing().when(service).grade(id);
        ResponseEntity<String> reply = subject.grade(id);

        assertEquals(HttpStatus.CREATED, reply.getStatusCode());
        assertEquals("grading completed", reply.getBody());
    }



    @Test
    void gradeShouldReturnNotModifiedOnNotOwnerException()
            throws NotOwnerException {
        long id = 1;
        doThrow(new NotOwnerException()).when(service).grade(id);
        ResponseEntity<String> reply = subject.grade(id);
        assertEquals(HttpStatus.NOT_MODIFIED, reply.getStatusCode());
    }

    @Test
    void getAssignment() {
        long id = 1;
        Assignment assignment = new Assignment();
        given(service.getAssignment(id)).willReturn(assignment);

        ResponseEntity<Assignment> reply = subject.getAssignment(id);

        assertEquals(HttpStatus.OK, reply.getStatusCode());
        assertEquals(reply.getBody(), assignment);
    }
}