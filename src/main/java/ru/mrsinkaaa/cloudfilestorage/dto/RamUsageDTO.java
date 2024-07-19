package ru.mrsinkaaa.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RamUsageDTO {

    private long ownerId;
    private long totalUsedRam;

}
