package ai.tecton.client.utils;

import ai.tecton.client.request.GetFeaturesRequestData;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class TestUtils {

  public static List<String> readAllFilesInDirectory(String directoryPath, String fileExtension)
      throws IOException, URISyntaxException {
    List<String> stringList = new ArrayList<>();
    URL url = TestUtils.class.getClassLoader().getResource(directoryPath);
    Path path = Paths.get(url.toURI());
    Files.walk(path, 1)
        .filter(file -> StringUtils.endsWith(file.getFileName().toString(), fileExtension))
        .sorted()
        .forEach(
            file -> {
              try {
                stringList.add(new String(Files.readAllBytes(file)));
              } catch (IOException ignored) {
              }
            });
    return stringList;
  }

  public static List<GetFeaturesRequestData> generateFraudRequestDataFromFile(String filePath)
      throws IOException {
    List<GetFeaturesRequestData> requestDataList = new ArrayList<>();
    File file = new File(TestUtils.class.getClassLoader().getResource(filePath).getFile());
    String content = new String(Files.readAllBytes(file.toPath()));
    Arrays.asList(StringUtils.split(content, "\n"))
        .forEach(
            row -> {
              String[] vals = StringUtils.split(row, ",");
              requestDataList.add(
                  new GetFeaturesRequestData()
                      .addJoinKey("user_id", vals[0])
                      .addJoinKey("merchant", vals[2])
                      .addRequestContext("amt", Double.parseDouble(vals[1])));
            });
    return requestDataList;
  }

  public static List<GetFeaturesRequestData> generateRequestDataForSize(int size) {
    List<GetFeaturesRequestData> requestDataList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      String key = RandomStringUtils.randomAlphanumeric(5);
      String val = RandomStringUtils.randomAlphanumeric(5);
      requestDataList.add(new GetFeaturesRequestData().addJoinKey(key, val));
    }
    return requestDataList;
  }
}
