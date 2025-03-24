package edu.upb.tresenraya.utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundUtils {

    public static void playSound(String resourcePath) {
        try {
            URL soundURL = SoundUtils.class.getResource(resourcePath);
            if (soundURL == null) {
                System.err.println("No se encontró el recurso de sonido: " + resourcePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error al reproducir sonido: " + e.getMessage());
        }
    }
}
