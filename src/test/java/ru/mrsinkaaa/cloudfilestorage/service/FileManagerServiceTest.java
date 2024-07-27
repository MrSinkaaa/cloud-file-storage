//package ru.mrsinkaaa.cloudfilestorage.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.multipart.MultipartFile;
//import ru.mrsinkaaa.cloudfilestorage.dto.RamUsageDTO;
//import ru.mrsinkaaa.cloudfilestorage.entity.File;
//import ru.mrsinkaaa.cloudfilestorage.entity.Folder;
//import ru.mrsinkaaa.cloudfilestorage.entity.User;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//public class FileManagerServiceTest {
//
//    @Mock
//    private FileService fileService;
//
//    @Mock
//    private FolderService folderService;
//
//    @Mock
//    private MinioService minioService;
//
//    @InjectMocks
//    private FileManagerService fileManagerService;
//
//    private User owner;
//    private Folder parentFolder;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        System.out.println("Running setup before each test...");
//
//        owner = new User();
//        owner.setId(1L);
//        owner.setUsername("testuser");
//
//        parentFolder = new Folder();
//        parentFolder.setFolderName("parent/");
//        parentFolder.setMinioObjectId("parent/");
//    }
//
//    @Test
//    void testUploadFile() {
//        MultipartFile uploadFile = mock(MultipartFile.class);
//        when(uploadFile.getOriginalFilename()).thenReturn("testfile.txt");
//        when(uploadFile.getSize()).thenReturn(1024L);
//        when(uploadFile.getContentType()).thenReturn("text/plain");
//
//        when(folderService.findByFolderName(anyString())).thenReturn(parentFolder);
//        when(fileService.save(any(File.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        File savedFile = fileManagerService.uploadFile(owner, uploadFile, "testfolder");
//
//        assertNotNull(savedFile);
//        assertEquals("testfile.txt", savedFile.getFileName());
//        verify(minioService).uploadFile(uploadFile, "parent/testfile.txt");
//    }
//
//
//    @Test
//    void testUploadFolder_Success() {
//        when(folderService.findByOwnerAndFolderName(owner, "parent/")).thenReturn(parentFolder);
//
//        MultipartFile file1 = mock(MultipartFile.class);
//        when(file1.getOriginalFilename()).thenReturn("subfolder1/file1.txt");
//
//        MultipartFile file2 = mock(MultipartFile.class);
//        when(file2.getOriginalFilename()).thenReturn("subfolder2/file2.txt");
//
//        MultipartFile[] files = new MultipartFile[]{file1, file2};
//
//        Folder subfolder1 = new Folder();
//        subfolder1.setFolderName("subfolder1/");
//        subfolder1.setMinioObjectId("parent/subfolder1/");
//
//        Folder subfolder2 = new Folder();
//        subfolder2.setFolderName("subfolder2/");
//        subfolder2.setMinioObjectId("parent/subfolder2/");
//
//        when(folderService.findByFolderName("subfolder1/")).thenReturn(subfolder1);
//        when(folderService.findByFolderName("subfolder2/")).thenReturn(subfolder2);
//
//        when(folderService.createFolder(any(User.class), eq("subfolder1/"), eq("parent/"))).thenReturn(subfolder1);
//        when(folderService.createFolder(any(User.class), eq("subfolder2/"), eq("parent/"))).thenReturn(subfolder2);
//
//        Folder result = fileManagerService.uploadFolder(owner, files, "parent/");
//
//        assertNotNull(result);
//        verify(fileService, times(2)).save(any(File.class)); // Verify the save method is called on the fileService twice
//        verify(minioService, times(2)).uploadFile(any(MultipartFile.class), anyString()); // Verify the files are uploaded twice
//    }
//
//    @Test
//    void testUploadFolder_CreateSubfolders() {
//        Folder subfolder = new Folder();
//        subfolder.setFolderName("subfolder/");
//        subfolder.setMinioObjectId("parent/subfolder/");
//
//        Folder subsubfolder = new Folder();
//        subsubfolder.setFolderName("subsubfolder/");
//        subsubfolder.setMinioObjectId("parent/subfolder/subsubfolder/");
//
//        when(folderService.findByOwnerAndFolderName(owner, "parent/")).thenReturn(parentFolder);
//        when(folderService.findByOwnerAndFolderName(owner, "subfolder/")).thenReturn(subfolder);
//
//        MultipartFile file = mock(MultipartFile.class);
//        when(file.getOriginalFilename()).thenReturn("subfolder/subsubfolder/file.txt");
//
//        MultipartFile[] files = new MultipartFile[]{file};
//
//        when(folderService.createFolder(any(User.class), eq("subfolder/"), eq("parent/"))).thenReturn(subfolder);
//        when(folderService.createFolder(any(User.class), eq("subsubfolder/"), eq("subfolder/"))).thenReturn(subsubfolder);
//        when(folderService.findByFolderName("subsubfolder/")).thenReturn(subsubfolder);
//
//        Folder result = fileManagerService.uploadFolder(owner, files, "parent/");
//
//        assertNotNull(result);
//        verify(fileService, times(1)).save(any(File.class)); // Verify the save method is called on the fileService
//        verify(minioService, times(1)).uploadFile(file, "parent/subfolder/subsubfolder/file.txt"); // Verify the file is uploaded
//    }
//
//}
