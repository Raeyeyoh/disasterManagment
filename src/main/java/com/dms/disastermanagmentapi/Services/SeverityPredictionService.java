package com.dms.disastermanagmentapi.Services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeverityPredictionService {

    public String predictSeverity(String incidentType, int descriptionLength, String region, int regionRisk) {
        try {
            String jsonInput = String.format(
                "{\"incident_type\":\"%s\",\"description_length\":%d,\"region\":\"%s\",\"region_risk\":%d}",
                incidentType, descriptionLength, region, regionRisk
            );
String jsonInputEscaped = jsonInput.replace("\"", "\\\"");

            ProcessBuilder pb = new ProcessBuilder(
                "C:\\Users\\raeye\\PyCharmProjects\\drm_ai\\.venv\\Scripts\\python.exe",
                "C:\\Users\\raeye\\PyCharmProjects\\drm_ai\\predict_severity.py",
                jsonInputEscaped
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            process.waitFor();

            String predictedSeverity = "MEDIUM";
            for (int i = lines.size() - 1; i >= 0; i--) {
                if (!lines.get(i).trim().isEmpty()) {
                    predictedSeverity = lines.get(i).trim().replaceAll("[^A-Za-z]", "");
                    break;
                }
            }

            return predictedSeverity;

        } catch (Exception e) {
            e.printStackTrace();
            return "MEDIUM";
        }
    }
}
