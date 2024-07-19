package ru.mrsinkaaa.cloudfilestorage.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RamConfig {

    private final long availableRamInBytes;

    public RamConfig(@Value("${ram.available}") long availableRamInMb) {
        //Convert MB to bytes
        this.availableRamInBytes = availableRamInMb * 1024 * 1024;
    }

}
