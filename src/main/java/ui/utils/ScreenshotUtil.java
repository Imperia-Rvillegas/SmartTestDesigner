package ui.utils;

import config.*;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import io.cucumber.java.Scenario;
import ui.manager.PageManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilidad para capturar screenshots y adjuntarlos al reporte HTML de Cucumber.
 */
public class ScreenshotUtil {

    private final WebDriver driver;
    private final ScenarioContext scenarioContext;

    public ScreenshotUtil(PageManager pageManager) {
        this.driver = pageManager.getDriver();
        this.scenarioContext = pageManager.getScenarioContext();
    }

    /**
     * Captura y adjunta usando el Scenario almacenado en ScenarioContext.
     */
    public void capture(String stepName) {
        Scenario scenario = scenarioContext.getScenario();
        captureWithScenario(scenario, stepName);
    }

    /**
     * Captura y adjunta pasando directamente el Scenario.
     */
    public void captureWithScenario(Scenario scenario, String stepName) {
        if (driver == null) {
            LogUtil.warn("Driver es null, no se puede capturar pantalla.");
            return;
        }
        if (scenario == null) {
            LogUtil.warn("Scenario es null, no se puede adjuntar imagen.");
            return;
        }

        String scenarioFolderPath = hooks.Hooks.getScenarioFolderPath();
        if (scenarioFolderPath == null || scenarioFolderPath.isEmpty()) {
            LogUtil.warn("scenarioFolderPath no está definido.");
            return;
        }

        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String cleanStepName = stepName.replaceAll("[^a-zA-Z0-9]", "_");
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String fileName = cleanStepName + "_" + timestamp + ".png";

            File dest = new File(scenarioFolderPath + fileName);
            FileUtils.copyFile(src, dest);

            byte[] fileContent = FileUtils.readFileToByteArray(dest);
            scenario.attach(fileContent, "image/png", fileName);

            LogUtil.info("Screenshot capturado y adjuntado: " + fileName);
        } catch (IOException e) {
            LogUtil.error("Error al capturar o adjuntar screenshot", e);
        }
    }
}