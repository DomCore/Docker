package com.universalna.nsds.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.imageio.ImageIO;

@Configuration
public class ImageIoConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        ImageIO.scanForPlugins();
    }
}
