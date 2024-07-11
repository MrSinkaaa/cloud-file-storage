package ru.mrsinkaaa.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.mrsinkaaa.cloudfilestorage.dto.FileDTO;
import ru.mrsinkaaa.cloudfilestorage.dto.FolderDTO;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class FileManagerServiceDeleteFolderTest {

    @Mock
    private FolderService folderService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileManagerService fileManagerService;

    private User owner;
    private Folder folder;
    private List<FileDTO> filesToDelete;
    private List<FolderDTO> subFoldersDTO;
    private List<Folder> subFolders;
    private List<FileDTO> filesInFolder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Initialize test data
        owner = new User();
        owner.setId(1L);

        folder = new Folder();
        folder.setId(2L);

        filesToDelete = new ArrayList<>();
        FileDTO file1 = FileDTO.builder()
                .name("file1")
                .id(1L)
                .build();
        filesToDelete.add(file1);

        FileDTO file2 = FileDTO.builder()
                .name("file2")
                .id(2L)
                .build();
        filesToDelete.add(file2);

        subFoldersDTO = new ArrayList<>();
        FolderDTO subFolder1 = FolderDTO.builder()
                .id(3L)
                .build();
        subFoldersDTO.add(subFolder1);

        FolderDTO subFolder2 = FolderDTO.builder()
                .id(4L)
                .build();
        subFoldersDTO.add(subFolder2);

        FolderDTO subFolder3 = FolderDTO.builder()
                .id(5L)
                .build();
        subFoldersDTO.add(subFolder3);

        subFolders = subFoldersDTO.stream().map(item ->
                Folder.builder().id(item.getId()).build()).collect(toList());

        filesInFolder = new ArrayList<>();
        for(long i = 2; i <= 8; i++) {
            FileDTO file = FileDTO.builder()
                   .name("file" + i)
                    .id(i)
                   .build();
            filesInFolder.add(file);
        }
    }

    @Test
    public void testDeleteFolder_Success() {
        // Define mock behavior
        when(folderService.findByFolderId(anyLong())).thenReturn(folder);
        when(folderService.findSubFolders(anyLong())).thenReturn(subFoldersDTO);
        when(fileService.findByFolderId(folder)).thenReturn(filesToDelete);

        // Call the method under test
        fileManagerService.deleteFolder(owner, folder.getId());

        // Verify interactions
        verify(fileService, times(2)).deleteFile(eq(owner), anyLong());
        verify(folderService, times(1)).deleteFolder(eq(owner), eq(folder));
    }

    @Test
    public void testDeleteFolder_NoFilesInFolder() {
        // Define mock behavior
        when(folderService.findByFolderId(anyLong())).thenReturn(folder);
        when(folderService.findSubFolders(anyLong())).thenReturn(subFoldersDTO);
        when(fileService.findByFolderId(folder)).thenReturn(new ArrayList<>());

        // Call the method under test
        fileManagerService.deleteFolder(owner, folder.getId());

        // Verify interactions
        verify(fileService, times(0)).deleteFile(eq(owner), anyLong());
        verify(folderService, times(1)).deleteFolder(eq(owner), eq(folder));
    }

    @Test
    public void testDeleteFolder_NoSubFolders() {
        // Define mock behavior
        when(folderService.findByFolderId(anyLong())).thenReturn(folder);
        when(folderService.findSubFolders(anyLong())).thenReturn(new ArrayList<>());
        when(fileService.findByFolderId(folder)).thenReturn(filesToDelete);

        // Call the method under test
        fileManagerService.deleteFolder(owner, folder.getId());

        // Verify interactions
        verify(fileService, times(2)).deleteFile(eq(owner), anyLong());
        verify(folderService, times(1)).deleteFolder(eq(owner), eq(folder));
    }

    @Test
    public void testDeleteFolder_NoFilesInSubFolders() {
        // Define mock behavior
        when(folderService.findByFolderId(anyLong())).thenReturn(folder);
        when(folderService.findSubFolders(anyLong())).thenReturn(subFoldersDTO);
        when(fileService.findByFolderId(folder)).thenReturn(new ArrayList<>());

        // Call the method under test
        fileManagerService.deleteFolder(owner, folder.getId());

        // Verify interactions
        verify(fileService, times(0)).deleteFile(eq(owner), anyLong());
        verify(folderService, times(1)).deleteFolder(eq(owner), eq(folder));
    }

    @Test
    public void testDeleteFolder_ManySubFolders() {
        // Mocking multiple subfolders, each with files
        List<FileDTO> filesInSubFolder1 = new ArrayList<>();
        filesInSubFolder1.add(filesInFolder.get(0));
        filesInSubFolder1.add(filesInFolder.get(1));

        List<FileDTO> filesInSubFolder2 = new ArrayList<>();
        filesInSubFolder2.add(filesInFolder.get(2));
        filesInSubFolder2.add(filesInFolder.get(3));

        List<FileDTO> filesInSubFolder3 = new ArrayList<>();
        filesInSubFolder3.add(filesInFolder.get(4));
        filesInSubFolder3.add(filesInFolder.get(5));

        // Define mock behavior
        when(folderService.findByFolderId(anyLong())).thenReturn(folder);
        when(folderService.findSubFolders(folder.getId())).thenReturn(subFoldersDTO);
        when(fileService.findByFolderId(folder)).thenReturn(filesToDelete);
        when(fileService.findByFolderId(subFolders.get(0))).thenReturn(filesInSubFolder1);
        when(fileService.findByFolderId(subFolders.get(1))).thenReturn(filesInSubFolder2);
        when(fileService.findByFolderId(subFolders.get(2))).thenReturn(filesInSubFolder3);

        // Call the method under test
        fileManagerService.deleteFolder(owner, folder.getId());

        // Verify interactions
        verify(fileService, times(2)).deleteFile(eq(owner), anyLong()); // From main folder
        verify(fileService, times(2)).deleteFile(eq(owner), anyLong()); // From subFolder1
        verify(fileService, times(2)).deleteFile(eq(owner), anyLong()); // From subFolder2
        verify(fileService, times(2)).deleteFile(eq(owner), anyLong()); // From subFolder3

        verify(folderService, times(1)).deleteFolder(eq(owner), eq(folder));
    }
}

