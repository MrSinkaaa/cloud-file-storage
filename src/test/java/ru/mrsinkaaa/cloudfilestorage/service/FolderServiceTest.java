package ru.mrsinkaaa.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
import ru.mrsinkaaa.cloudfilestorage.entity.User;
import ru.mrsinkaaa.cloudfilestorage.exception.FolderNotFoundException;
import ru.mrsinkaaa.cloudfilestorage.repository.FolderRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private FileService fileService;

    @Mock
    private FolderService folderService;

    @InjectMocks
    private FileManagerService fileManagerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        System.out.println("Running setup before each test...");
    }

    @Test
    void testUploadFolder_Success() {
        User owner = new User();
        owner.setUsername("testuser");

        Folder parentFolder = new Folder();
        parentFolder.setFolderName("parent/");
        parentFolder.setMinioObjectId("parent/");

        when(folderRepository.findByOwnerAndFolderName(owner, "parent/"))
                .thenReturn(Optional.of(parentFolder));

        MultipartFile file1 = mock(MultipartFile.class);
        when(file1.getOriginalFilename()).thenReturn("subfolder1/file1.txt");

        MultipartFile file2 = mock(MultipartFile.class);
        when(file2.getOriginalFilename()).thenReturn("subfolder2/file2.txt");

        MultipartFile[] files = new MultipartFile[]{file1, file2};

        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Folder result = fileManagerService.uploadFolder(owner, files, "parent/");

        assertNotNull(result);
        verify(fileManagerService, times(1)).uploadFile(owner, file1, "subfolder1/");
        verify(fileManagerService, times(1)).uploadFile(owner, file2, "subfolder2/");
    }

    @Test
    void testUploadFolder_ParentFolderNotFound() {
        User owner = new User();
        owner.setUsername("testuser");

        when(folderRepository.findByOwnerAndFolderName(owner, "parent/"))
                .thenReturn(Optional.empty());

        MultipartFile file1 = mock(MultipartFile.class);
        when(file1.getOriginalFilename()).thenReturn("subfolder1/file1.txt");

        MultipartFile file2 = mock(MultipartFile.class);
        when(file2.getOriginalFilename()).thenReturn("subfolder2/file2.txt");

        MultipartFile[] files = new MultipartFile[]{file1, file2};

        assertThrows(FolderNotFoundException.class, () -> {
            fileManagerService.uploadFolder(owner, files, "parent/");
        });

        verify(fileManagerService, never()).uploadFile(any(User.class), any(MultipartFile.class), anyString());
    }

    @Test
    void testUploadFolder_CreateSubfolders() {
        User owner = new User();
        owner.setUsername("testuser");

        Folder parentFolder = new Folder();
        parentFolder.setFolderName("parent/");
        parentFolder.setMinioObjectId("parent/");

        Folder subfolder = new Folder();
        subfolder.setFolderName("subfolder/");
        subfolder.setMinioObjectId("parent/subfolder/");

        when(folderRepository.findByOwnerAndFolderName(owner, "parent/"))
                .thenReturn(Optional.of(parentFolder));

        when(folderRepository.findByOwnerAndFolderName(owner, "subfolder/"))
                .thenReturn(Optional.of(subfolder));


        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("subfolder/subsubfolder/file.txt");

        MultipartFile[] files = new MultipartFile[]{file};

        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Folder result = fileManagerService.uploadFolder(owner, files, "parent/");

        assertNotNull(result);
        verify(fileManagerService, times(1)).uploadFile(owner, file, "subsubfolder/");
    }
}
