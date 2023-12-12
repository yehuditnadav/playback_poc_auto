package org.example;

import il.co.topq.difido.ReportManager;
import il.co.topq.difido.model.Enums;
import net.sourceforge.tess4j.Word;
import org.sikuli.script.Location;
import org.sikuli.script.Screen;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Listeners({il.co.topq.difido.ReportManagerHook.class})
public class PlaybackPoc {

    private ReportManager report = ReportManager.getInstance();

    private Terminal terminal = new Terminal();

    private TakeScreenshotService.WindowInfo emulatorWindow;

    private Screen screen = new Screen();

    @Test
    public void verifyPlayback() throws Exception {
        getEmulatorWindow();
        playMediaInEmulator();
        verifyPlaybackInEmulator();
    }

    private void getEmulatorWindow() throws Exception {
        String deviceWindowTitle = System.getProperty("deviceWindowTitle") != null ?
                System.getProperty("deviceWindowTitle") : "Running Devices - My Application";
        report.log("Find emulator window by title: " + deviceWindowTitle);
        int hWnd = TakeScreenshotService.User32.instance.FindWindowA(null, deviceWindowTitle);
        if (hWnd == 0) {
            throw new Exception("Cant find emulator window by title: " + deviceWindowTitle);
        }
        emulatorWindow = TakeScreenshotService.getWindowInfo(hWnd);
    }

    private void verifyPlaybackInEmulator() throws Exception {
        report.startLevel("Verify media playback");
        try {
            String playbackDurationInMinutes = System.getProperty("playbackDurationInMinutes") != null ?
                    System.getProperty("playbackDurationInMinutes") : "2";
            String playbackPeriodInSeconds = System.getProperty("playbackPeriodInSeconds") != null ?
                    System.getProperty("playbackPeriodInSeconds") : "10";
            long startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            long timeout = Integer.parseInt(playbackDurationInMinutes) * 60 * 1000;
            int i = 0;
            while (currentTime - startTime < timeout) {
                String screen1ImgFileName = "screen1" + i + ".png";
                String screen2ImgFileName = "screen2" + i + ".png";
                takeScreenShot(screen1ImgFileName);
                delay(Integer.parseInt(playbackPeriodInSeconds) * 1000);
                takeScreenShot(screen2ImgFileName);
                double diff = ImageUtils.compareImages(screen1ImgFileName, screen2ImgFileName);
                if (diff > 0.0) {
                    report.log("Playback is running, as expected");
                } else {
                    report.log("Playback is not running, not as expected", Enums.Status.failure);
                }
                currentTime = System.currentTimeMillis();
                i++;
            }
        } finally {
            report.endLevel();
        }
    }

    private void takeScreenShot(String imgFileName) throws Exception {
        TakeScreenshotService.User32.instance.SetForegroundWindow(emulatorWindow.hwnd);
        BufferedImage createScreenCapture = new Robot().createScreenCapture(new Rectangle(emulatorWindow.rect.left,
                emulatorWindow.rect.top, (3*(emulatorWindow.rect.right/4)), (2*(emulatorWindow.rect.bottom/3))));
        File imageFile = new File(imgFileName);
        ImageIO.write(createScreenCapture, "png", imageFile);
        report.addImage(imageFile, imgFileName);
    }

    private void playMediaInEmulator() throws Exception {
        report.startLevel("Play media");
        try {
            report.step("Start Reshet application");
            terminal.sendCommand("adb logcat -c"); //clear logcat before start
            terminal.sendCommand("adb shell am start -n com.applicaster.iReshet/com.reshet.sott.ui.splash.SplashActivity");
            report.step("Waiting for home screen");
            waitForLogcatMessage("Displayed com.applicaster.iReshet/com.reshet.sott.ui.home.HomeActivity");
            report.step("Play media");
//            playLive();
            playCNN();
//            playReality();
//            playVacation();

            report.step("Waiting for playback");
            waitForLogcatMessage("PKYouboraPlayerAdapter: getPlayrate currentPlaybackRate = 1.0");
        } finally {
            report.endLevel();
        }
    }

    private void playLive() throws Exception {
        terminal.sendCommand("adb shell input keyevent 23"); //KEYCODE_DPAD_CENTER

//        selectChannelInHomeScreen("העדכונים");
    }

    private void playCNN() throws Exception {
        selectChannelInHomeScreen("בשידור");
        report.log("Clicking on 'צפו עכשיו'");
        Word word = getWordInScreen("cnnScreen.png", "צפו");
        double liveX = ((word.getBoundingBox().getMinX() + word.getBoundingBox().getMaxX()) / 2);
        double liveY = ((word.getBoundingBox().getMinY() + word.getBoundingBox().getMaxY()) / 2);
        screen.click(new Location(liveX, liveY));
//        terminal.sendCommand("adb shell input tap " + liveX + " " + liveY);
    }

    private void playReality() throws Exception {
        selectChannelInHomeScreen("ריאליטי");
    }

    private void playVacation() throws Exception {
        selectChannelInHomeScreen("נופש");
    }

    private void selectChannelInHomeScreen(String channel) throws Exception {
        Word word = getWordInScreen("homeScreen.png", channel);
        double liveX = ((word.getBoundingBox().getMinX() + word.getBoundingBox().getMaxX()) / 2);
        double liveY = ((word.getBoundingBox().getMinY() + word.getBoundingBox().getMaxY()) / 2);
        report.log("Double clicking on channel");
        screen.doubleClick(new Location(liveX, liveY));
        delay(1000);
    }

    private void waitForLogcatMessage(String expectedLogMessage) throws Exception {
        while (!terminal.getLastCommandResult().contains(expectedLogMessage)) {
            terminal.sendCommand("adb logcat -d | find \"" + expectedLogMessage + "\"");
            delay(5000);
        }
        delay(1000);
    }

    private void delay(long millis) throws Exception {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        report.log("Waiting for " + ((minutes > 0) ? (minutes + " minutes ") : "") + ((seconds > 0) ? (seconds + " seconds") : ""));
        Thread.sleep(millis);
    }

    private Word getWordInScreen(String screenImageFileName, String word) throws Exception {
        takeScreenShot(screenImageFileName);
        Word wordObject = OcrService.getWord(screenImageFileName, word);
        if (wordObject == null) {
            delay(1000);
            takeScreenShot(screenImageFileName);
            wordObject = OcrService.getWord(screenImageFileName, word);
        }
        return wordObject;
    }
}