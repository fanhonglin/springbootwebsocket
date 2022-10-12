import com.suyu.websocket.Application;
import com.suyu.websocket.engine.IatClientMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class IatEngineTest {

//    private static final String SERVER = "124.243.226.40:17022";

    private static final String SERVER = "10.40.7.30:9097";

    //单个文件测试
    private final static String AUDIO_PATH = "D:\\work\\git\\springbootwebsocket\\src\\main\\resources\\16k.wav";

//    private final static String AUDIO_PATH = "D:\\work\\git\\springbootwebsocket\\src\\main\\resources\\qyhtest-0001.wav";

    private static final String sampleRate = "16k";


    @Test
    public void fileTest() throws Exception {
        IatClientMethod iatClientTest = new IatClientMethod();
        List<String> files = new ArrayList<>();
        files.add(AUDIO_PATH);

        for (String audio : files) {
            File file = new File(audio);
            if (!file.exists()) {
                log.error("音频文件不存在. file:{}", AUDIO_PATH);
                return;
            }
            File outfile = new File(file.getAbsolutePath() + ".txt");
            iatClientTest.doConvert(SERVER, file, sampleRate, outfile);
        }


        while (true) {
            Thread.sleep(1);
        }

    }
}
