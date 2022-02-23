package core.basesyntax.service.impl;

import core.basesyntax.db.Storage;
import core.basesyntax.model.Apple;
import core.basesyntax.model.Banana;
import core.basesyntax.model.Fruit;
import core.basesyntax.model.Orange;
import core.basesyntax.model.dto.FruitDto;
import core.basesyntax.service.DataConverterService;
import core.basesyntax.service.FileWriterService;
import core.basesyntax.service.StorageService;
import core.basesyntax.strategy.TransDistrStrategy;
import core.basesyntax.strategy.TransDistrStrategyImpl;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileWriterServiceImplTest {
    private static final int DEFAULT_QUANTITY = 0;
    private static final String TEST_REPORT_ACTUAL_PATH = "src/test/resources/testReportActual.csv";
    private static final String TEST_REPORT_EXPECTED_PATH =
            "src/test/resources/testReportExpected.scv";
    private static final String READ_DATA_PATH = "src/test/resources/input.csv";
    private static final String PURCHASE = "p";
    private static final String RETURN = "r";
    private static final String SUPPLY = "s";
    private static final String BALANCE = "b";
    private FileWriterService fileWriterService;

    @BeforeClass
    public static void setup() throws IOException {
        String stringInputData = "type,fruit,quantity" + System.lineSeparator()
                + "b,banana,20" + System.lineSeparator()
                + "b,apple,100" + System.lineSeparator()
                + "b,orange,50" + System.lineSeparator()
                + "s,banana,100" + System.lineSeparator()
                + "p,banana,13" + System.lineSeparator()
                + "r,apple,10" + System.lineSeparator()
                + "s,orange,50" + System.lineSeparator()
                + "p,orange,15" + System.lineSeparator()
                + "p,apple,20" + System.lineSeparator()
                + "p,banana,5" + System.lineSeparator()
                + "r,orange,5" + System.lineSeparator()
                + "s,banana,50";
        BufferedWriter fewLinesWriter = new BufferedWriter(new FileWriter(READ_DATA_PATH));
        fewLinesWriter.write(stringInputData);
        fewLinesWriter.close();
        String stringExpectedData = "fruit,quantity" + System.lineSeparator()
                + "banana,152" + System.lineSeparator()
                + "apple,90" + System.lineSeparator()
                + "orange,90";
        BufferedWriter expectedDataWriter =
                new BufferedWriter(new FileWriter(TEST_REPORT_EXPECTED_PATH));
        expectedDataWriter.write(stringExpectedData);
        expectedDataWriter.close();
    }

    @Before
    public void init() {
        Fruit apple = new Apple("apple");
        Storage.shopStorage.put(apple, DEFAULT_QUANTITY);
        Fruit banana = new Banana("banana");
        Storage.shopStorage.put(banana, DEFAULT_QUANTITY);
        Fruit orange = new Orange("orange");
        Storage.shopStorage.put(orange, DEFAULT_QUANTITY);
        Map<String, StorageService> operationDistributionMap = new HashMap<>();
        operationDistributionMap.put(PURCHASE, new StorageReductionServiceImpl());
        operationDistributionMap.put(RETURN, new StorageAdditionServiceImpl());
        operationDistributionMap.put(SUPPLY, new StorageAdditionServiceImpl());
        operationDistributionMap.put(BALANCE, new StorageAdditionServiceImpl());
        DataConverterService dataConverterService = new DataConverterServiceImpl();
        TransDistrStrategy transDistrStrategy =
                new TransDistrStrategyImpl(operationDistributionMap);
        DataReaderServiceImpl dataReaderService = new DataReaderServiceImpl();
        List<String> listStringData = dataReaderService.readDataFromFile(READ_DATA_PATH);
        List<FruitDto> operationsList = dataConverterService.convertDto(listStringData);
        new IteratorServiceImpl().iterate(operationsList, transDistrStrategy, Storage.shopStorage);
        fileWriterService = new FileWriterServiceImpl();
    }

    @Test
    public void writeToFile_correctData_ok() {
        fileWriterService.writeToFile(TEST_REPORT_ACTUAL_PATH, Storage.shopStorage);
        try {
            Assert.assertEquals(Files.readString(Path.of(TEST_REPORT_EXPECTED_PATH)),
                    Files.readString(Path.of(TEST_REPORT_ACTUAL_PATH)));
        } catch (IOException e) {
            throw new RuntimeException("Something wrong with test files: ", e);
        }
    }

    @Test (expected = RuntimeException.class)
    public void writeToFile_nullOutPath_notOk() {
        fileWriterService.writeToFile(null, Storage.shopStorage);
    }

    @AfterClass
    public static void tearDown() {
        Storage.shopStorage.clear();
        try {
            new FileWriter(TEST_REPORT_ACTUAL_PATH, false).close();
        } catch (IOException e) {
            throw new RuntimeException("Can't clear output file" + TEST_REPORT_ACTUAL_PATH);
        }
        try {
            new FileWriter(READ_DATA_PATH, false).close();
        } catch (IOException e) {
            throw new RuntimeException("Can't clear output file" + READ_DATA_PATH);
        }
        try {
            new FileWriter(TEST_REPORT_EXPECTED_PATH, false).close();
        } catch (IOException e) {
            throw new RuntimeException("Can't clear output file" + TEST_REPORT_EXPECTED_PATH);
        }
    }
}
