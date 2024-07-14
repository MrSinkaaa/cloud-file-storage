package ru.mrsinkaaa.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class FileManagerServiceRenameFolderTest {

    @Mock
    private FolderService folderService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileManagerService fileManagerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRenameFolder_NoSubfoldersOrFiles() {
        User owner = new User();
        Long folderId = 1L;
        String newFolderName = "newFolderName";

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setFolderName("oldFolderName");

        when(folderService.findByFolderId(folderId)).thenReturn(folder);
        when(folderService.findSubFolders(folderId)).thenReturn(new ArrayList<>());
        when(fileManagerService.getFilesByFolder(folderId)).thenReturn(new ArrayList<>());

        Folder renamedFolder = fileManagerService.renameFolder(owner, folderId, newFolderName);

        assertEquals(folder, renamedFolder);
        verify(fileService, never()).renameFile(any(User.class), any(Long.class), any(String.class));
        verify(folderService, never()).renameFolder(any(User.class), any(Long.class), any(String.class));
    }

    @Test
    void testRenameFolder_WithSubfoldersAndFiles() {
        User owner = new User();
        Long folderId = 1L;
        String newFolderName = "newFolderName";

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setFolderName("oldFolderName");

        FileDTO file1 = FileDTO.builder()
                .id(1L)
                .minioObjectId("oldFolderName/file1.txt")
                .build();

        FolderDTO subFolder = FolderDTO.builder()
                .id(2L)
                .minioObjectId("oldFolderName/subFolder/")
                .build();

        List<FileDTO> files = new ArrayList<>();
        files.add(file1);

        List<FolderDTO> subFolders = new ArrayList<>();
        subFolders.add(subFolder);

        when(folderService.findByFolderId(folderId)).thenReturn(folder);
        when(folderService.findSubFolders(folderId)).thenReturn(subFolders);
        when(folderService.findSubFolders(subFolder.getId())).thenReturn(new ArrayList<>());
        when(fileManagerService.getFilesByFolder(folderId)).thenReturn(files);
        when(fileManagerService.getFilesByFolder(subFolder.getId())).thenReturn(new ArrayList<>());

        Folder renamedFolder = fileManagerService.renameFolder(owner, folderId, newFolderName);

        assertEquals(folder, renamedFolder);

        ArgumentCaptor<String> filePathCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileService).renameFile(eq(owner), eq(file1.getId()), filePathCaptor.capture());
        assertEquals("newFolderName/file1.txt", filePathCaptor.getValue());

        ArgumentCaptor<String> folderPathCaptor = ArgumentCaptor.forClass(String.class);
        verify(folderService).renameFolder(eq(owner), eq(subFolder.getId()), folderPathCaptor.capture());
        assertEquals("newFolderName/subFolder/", folderPathCaptor.getValue());
    }

    @Test
    void testRenameFolder_WithNestedSubfoldersAndFiles() {
        User owner = new User();
        Long folderId = 1L;
        String newFolderName = "newFolderName";

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setFolderName("oldFolderName");

        FileDTO file1 = FileDTO.builder()
                .id(1L)
                .minioObjectId("oldFolderName/file1.txt")
                .build();

        FolderDTO subFolder = FolderDTO.builder()
                .id(2L)
                .minioObjectId("oldFolderName/subFolder/")
                .build();

        FolderDTO nestedSubFolder = FolderDTO.builder()
                .id(3L)
                .minioObjectId("oldFolderName/subFolder/nestedSubFolder/")
                .build();

        List<FileDTO> files = new ArrayList<>();
        files.add(file1);

        List<FolderDTO> subFolders = new ArrayList<>();
        subFolders.add(subFolder);
        subFolders.add(nestedSubFolder);

        when(folderService.findByFolderId(folderId)).thenReturn(folder);
        when(folderService.findSubFolders(folderId)).thenReturn(subFolders);
        when(folderService.findSubFolders(subFolder.getId())).thenReturn(List.of(nestedSubFolder));
        when(fileManagerService.getFilesByFolder(folderId)).thenReturn(files);
        when(fileManagerService.getFilesByFolder(subFolder.getId())).thenReturn(new ArrayList<>());
        when(fileManagerService.getFilesByFolder(nestedSubFolder.getId())).thenReturn(new ArrayList<>());

        Folder renamedFolder = fileManagerService.renameFolder(owner, folderId, newFolderName);

        assertEquals(folder, renamedFolder);

        ArgumentCaptor<String> filePathCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileService).renameFile(eq(owner), eq(file1.getId()), filePathCaptor.capture());
        assertEquals("newFolderName/file1.txt", filePathCaptor.getValue());

        ArgumentCaptor<String> folderPathCaptor = ArgumentCaptor.forClass(String.class);
        verify(folderService).renameFolder(eq(owner), eq(subFolder.getId()), folderPathCaptor.capture());
        assertEquals("newFolderName/subFolder/", folderPathCaptor.getValue());

        verify(folderService).renameFolder(eq(owner), eq(nestedSubFolder.getId()), folderPathCaptor.capture());
        assertEquals("newFolderName/subFolder/nestedSubFolder/", folderPathCaptor.getValue());
    }

    @Test
    void testRenameFolder_NonExistentFolder() {
        User owner = new User();
        Long folderId = 1L;
        String newFolderName = "newFolderName";

        when(folderService.findByFolderId(folderId)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> fileManagerService.renameFolder(owner, folderId, newFolderName));
    }

    @Test
    void testRenameFolder_SpecialCharactersInName() {
        User owner = new User();
        Long folderId = 1L;
        String newFolderName = "new@Folder#Name";

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setFolderName("oldFolderName");

        FileDTO file1 = FileDTO.builder()
                .id(1L)
                .minioObjectId("oldFolderName/file1.txt")
                .build();

        List<FileDTO> files = new ArrayList<>();
        files.add(file1);

        when(folderService.findByFolderId(folderId)).thenReturn(folder);
        when(folderService.findSubFolders(folderId)).thenReturn(new ArrayList<>());
        when(fileManagerService.getFilesByFolder(folderId)).thenReturn(files);

        Folder renamedFolder = fileManagerService.renameFolder(owner, folderId, newFolderName);

        assertEquals(folder, renamedFolder);

        ArgumentCaptor<String> filePathCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileService).renameFile(eq(owner), eq(file1.getId()), filePathCaptor.capture());
        assertEquals("new@Folder#Name/file1.txt", filePathCaptor.getValue());
    }

    @Test
    void testRenameFolder_SameName() {
        User owner = new User();
        Long folderId = 1L;
        String newFolderName = "oldFolderName";

        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setFolderName(newFolderName);

        when(folderService.findByFolderId(folderId)).thenReturn(folder);

        Folder renamedFolder = fileManagerService.renameFolder(owner, folderId, newFolderName);

        assertEquals(folder, renamedFolder);
        verify(fileService, never()).renameFile(any(User.class), any(Long.class), any(String.class));
        verify(folderService, never()).renameFolder(any(User.class), any(Long.class), any(String.class));
    }

}
