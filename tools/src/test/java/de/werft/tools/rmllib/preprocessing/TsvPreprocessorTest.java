package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.general.Document;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TsvPreprocessorTest extends CsvPreprocessorTest {

    @Test
    public void testWithSublist() throws IOException, URISyntaxException {
        TsvPreprocessor processor = new TsvPreprocessor("", 3);
        URL inputFile = Paths.get("drive", "anno.tsv").toUri().toURL();
        Document doc = new Document(null, inputFile, null);
        URL actualInput = processor.preprocessInput(doc);
        List<String> lines = Files.readAllLines(Paths.get(actualInput.toURI()));

        for (String line : lines) {
            System.out.println(line + "\n");
        }
    }

    @Test
    public void testSublist() throws IOException, URISyntaxException {
        TsvPreprocessor processor = new TsvPreprocessor("", 1);
        URL inputFile = Paths.get("src", "test", "resources", "rml", "tsv", "test.tsv").toUri().toURL();
        Document doc = new Document(null, inputFile, null);
        URL actualInput = processor.preprocessInput(doc);
        List<String> lines = Files.readAllLines(Paths.get(actualInput.toURI()));

        for (String line : lines) {
            System.out.println(line);
        }
    }

    @Test
    public void testOnFile() throws IOException, URISyntaxException {
        TsvPreprocessor processor = new TsvPreprocessor("");
        URL inputFile = Paths.get("drive", "ada_level.tsv").toUri().toURL();
        Document doc = new Document(null, inputFile, null);
        URL actualInput = processor.preprocessInput(doc);
        List<String> lines = Files.readAllLines(Paths.get(actualInput.toURI()));
        System.out.println(lines);
    }

    @Test
    public void testInputPreprocessing() throws IOException, URISyntaxException {
        TsvPreprocessor processor = new TsvPreprocessor("");
        URL inputFile = createFile(input);
        Document doc = new Document(null, inputFile, null);
        URL actualInput = processor.preprocessInput(doc);
        Assert.assertTrue(isSameFileContent(actualInput));

        // cleanup
        Files.delete(Paths.get(inputFile.toURI()));
        Files.delete(Paths.get(actualInput.toURI()));
    }

    private boolean isSameFileContent(URL actualInput) throws URISyntaxException, IOException {
        byte[] actualContent = Files.readAllBytes(Paths.get(actualInput.toURI()));
        Assert.assertArrayEquals(expectedInput.getBytes(), actualContent);
        return true;
    }

    private String expectedInput =
            "Name;Source File;Clip;Duration;Tracks;Start;End;Fps;Original_video;Audio_format;Audio_sr;Audio_bit;Soundroll;Frame_width;Frame_height;Uuid;Sup_version;Exposure_index;Gamma;White_balance;Cc_shift;Look_name;Look_burned_in;Sensor_fps;Shutter_angle;Image_orientation;Pixelaspectratio;Manufacturer;Camera_model;Camera_sn;Camera_id;Camera_index;Project_fps;Sxs_sn;Production;Cinematographer;Operator;Director;Location;Company;User_info1;User_info2;Date_camera;Time_camera;Master_slave;Eye_index;Reel_name;Umid;Lens_type;Focus_distance_unit;Lens_sn;Take;Scene;Circle\n" +
                    "A001C001_150827_R2GJ;A001C001_150827_R2GJ.mov;001;00:00:20:02;V;14:39:39:09;14:39:59:11;24;ProRes 4444 (HD1080p);;;;NONE;1920;1080;313b70bc-0000-4000-952c-ae2e00000000;Alexa_11.0:29147;320;LOG-C;3200;+0;None;No;24.000;172.8;0;1.0;ARRI;Alexa Plus;3187;R2GJ;A;24.000;1a00000000000105;DWERFT_TESTDREH;ROBERT_FRIEBE;CEVAT_MASKAR;CEVAT_MASKAR;BERNSTEINSEE;FUB;;;20150827;14h37m15s;OFF;SINGLE;A001R2GJ;0x060A2B340101010501010F0013000000313B70BC00004000952CAE2E00000000;;Metric;0;;;No\n" +
                    "A001C002_150827_R2GJ;A001C002_150827_R2GJ.mov;002;00:00:21:01;V;14:40:25:10;14:40:46:11;24;ProRes 4444 (HD1080p);;;;NONE;1920;1080;b2bba60b-0000-4000-adf0-c5e800000000;Alexa_11.0:29147;320;LOG-C;3200;+0;None;No;24.000;172.8;0;1.0;ARRI;Alexa Plus;3187;R2GJ;A;24.000;1a00000000000105;DWERFT_TESTDREH;ROBERT_FRIEBE;CEVAT_MASKAR;CEVAT_MASKAR;BERNSTEINSEE;FUB;;;20150827;14h38m02s;OFF;SINGLE;A001R2GJ;0x060A2B340101010501010F0013000000B2BBA60B00004000ADF0C5E800000000;;Metric;0;;;No\n" +
                    "A001C003_150827_R2GJ;A001C003_150827_R2GJ.mov;003;00:00:03:08;V;14:45:26:04;14:45:29:12;24;ProRes 4444 (HD1080p);;;;NONE;1920;1080;0d5d416a-0000-4000-89d1-6b6300000000;Alexa_11.0:29147;320;LOG-C;5600;+0;None;No;24.000;172.8;0;1.0;ARRI;Alexa Plus;3187;R2GJ;A;24.000;1a00000000000105;DWERFT_TESTDREH;ROBERT_FRIEBE;CEVAT_MASKAR;CEVAT_MASKAR;BERNSTEINSEE;FUB;;;20150827;14h43m03s;OFF;SINGLE;A001R2GJ;0x060A2B340101010501010F00130000000D5D416A0000400089D16B6300000000;;Metric;0;;;No\n" +
                    "A001C004_150827_R2GJ;A001C004_150827_R2GJ.mov;004;00:00:02:07;V;14:47:00:16;14:47:02:23;24;ProRes 4444 (HD1080p);;;;NONE;1920;1080;e5b82767-0000-4000-b394-129b00000000;Alexa_11.0:29147;320;LOG-C;5600;+0;None;No;24.000;172.8;0;1.0;ARRI;Alexa Plus;3187;R2GJ;A;24.000;1a00000000000105;DWERFT_TESTDREH;ROBERT_FRIEBE;CEVAT_MASKAR;CEVAT_MASKAR;BERNSTEINSEE;FUB;;;20150827;14h44m38s;OFF;SINGLE;A001R2GJ;0x060A2B340101010501010F0013000000E5B8276700004000B394129B00000000;;Metric;0;;;No\n";

    private String input =
                    "Name\tSource File\tClip\tDuration\tTracks\tStart\tEnd\tFps\tOriginal_video\tAudio_format\tAudio_sr\tAudio_bit\tSoundroll\tFrame_width\tFrame_height\tUuid\tSup_version\tExposure_index\tGamma\tWhite_balance\tCc_shift\tLook_name\tLook_burned_in\tSensor_fps\tShutter_angle\tImage_orientation\tPixelaspectratio\tManufacturer\tCamera_model\tCamera_sn\tCamera_id\tCamera_index\tProject_fps\tSxs_sn\tProduction\tCinematographer\tOperator\tDirector\tLocation\tCompany\tUser_info1\tUser_info2\tDate_camera\tTime_camera\tMaster_slave\tEye_index\tReel_name\tUmid\tLens_type\tFocus_distance_unit\tLens_sn\tTake\tScene\tCircle\n" +
                    "A001C001_150827_R2GJ\tA001C001_150827_R2GJ.mov\t001\t00:00:20:02\tV\t14:39:39:09\t14:39:59:11\t24\tProRes 4444 (HD1080p)\t\t\t\tNONE\t1920\t1080\t313b70bc-0000-4000-952c-ae2e00000000\tAlexa_11.0:29147\t320\tLOG-C\t3200\t+0\tNone\tNo\t24.000\t172.8\t0\t1.0\tARRI\tAlexa Plus\t3187\tR2GJ\tA\t24.000\t1a00000000000105\tDWERFT_TESTDREH\tROBERT_FRIEBE\tCEVAT_MASKAR\tCEVAT_MASKAR\tBERNSTEINSEE\tFUB\t\t\t20150827\t14h37m15s\tOFF\tSINGLE\tA001R2GJ\t0x060A2B340101010501010F0013000000313B70BC00004000952CAE2E00000000\t\tMetric\t0\t\t\tNo\n" +
                    "A001C002_150827_R2GJ\tA001C002_150827_R2GJ.mov\t002\t00:00:21:01\tV\t14:40:25:10\t14:40:46:11\t24\tProRes 4444 (HD1080p)\t\t\t\tNONE\t1920\t1080\tb2bba60b-0000-4000-adf0-c5e800000000\tAlexa_11.0:29147\t320\tLOG-C\t3200\t+0\tNone\tNo\t24.000\t172.8\t0\t1.0\tARRI\tAlexa Plus\t3187\tR2GJ\tA\t24.000\t1a00000000000105\tDWERFT_TESTDREH\tROBERT_FRIEBE\tCEVAT_MASKAR\tCEVAT_MASKAR\tBERNSTEINSEE\tFUB\t\t\t20150827\t14h38m02s\tOFF\tSINGLE\tA001R2GJ\t0x060A2B340101010501010F0013000000B2BBA60B00004000ADF0C5E800000000\t\tMetric\t0\t\t\tNo\n" +
                    "A001C003_150827_R2GJ\tA001C003_150827_R2GJ.mov\t003\t00:00:03:08\tV\t14:45:26:04\t14:45:29:12\t24\tProRes 4444 (HD1080p)\t\t\t\tNONE\t1920\t1080\t0d5d416a-0000-4000-89d1-6b6300000000\tAlexa_11.0:29147\t320\tLOG-C\t5600\t+0\tNone\tNo\t24.000\t172.8\t0\t1.0\tARRI\tAlexa Plus\t3187\tR2GJ\tA\t24.000\t1a00000000000105\tDWERFT_TESTDREH\tROBERT_FRIEBE\tCEVAT_MASKAR\tCEVAT_MASKAR\tBERNSTEINSEE\tFUB\t\t\t20150827\t14h43m03s\tOFF\tSINGLE\tA001R2GJ\t0x060A2B340101010501010F00130000000D5D416A0000400089D16B6300000000\t\tMetric\t0\t\t\tNo\n" +
                    "A001C004_150827_R2GJ\tA001C004_150827_R2GJ.mov\t004\t00:00:02:07\tV\t14:47:00:16\t14:47:02:23\t24\tProRes 4444 (HD1080p)\t\t\t\tNONE\t1920\t1080\te5b82767-0000-4000-b394-129b00000000\tAlexa_11.0:29147\t320\tLOG-C\t5600\t+0\tNone\tNo\t24.000\t172.8\t0\t1.0\tARRI\tAlexa Plus\t3187\tR2GJ\tA\t24.000\t1a00000000000105\tDWERFT_TESTDREH\tROBERT_FRIEBE\tCEVAT_MASKAR\tCEVAT_MASKAR\tBERNSTEINSEE\tFUB\t\t\t20150827\t14h44m38s\tOFF\tSINGLE\tA001R2GJ\t0x060A2B340101010501010F0013000000E5B8276700004000B394129B00000000\t\tMetric\t0\t\t\tNo\n";
}