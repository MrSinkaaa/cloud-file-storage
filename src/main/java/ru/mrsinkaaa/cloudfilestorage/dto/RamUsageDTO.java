package ru.mrsinkaaa.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RamUsageDTO {

    private long ownerId;
    private long totalUsedRam;

}
