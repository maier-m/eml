package org.ecoinformatics.datamanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ecoinformatics.datamanager.database.DatabaseConnectionPoolInterfaceTest;
import org.ecoinformatics.datamanager.database.DatabaseLoader;
import org.ecoinformatics.datamanager.download.DataStorageTest;
import org.ecoinformatics.datamanager.download.EcogridEndPointInterfaceTest;
import org.ecoinformatics.datamanager.parser.DataPackage;

public class DataManagerTest extends TestCase {
  
  /*
   * Class fields
   */
  
  
  /*
   * Instance fields
   */
  
  private DataManager dataManager;
  private EcogridEndPointInterfaceTest endPointInfo = new EcogridEndPointInterfaceTest();
  
//  private final String TEST_DOCUMENT = "knb-lter-mcm.7002.1";
//  private final String TEST_SERVER = "http://metacat.lternet.edu/knb/metacat";
//  private final int ENTITY_NUMBER_EXPECTED = 5;

//  private final String TEST_DOCUMENT = "tao.1.1";
//  private final String TEST_SERVER ="http://knb.ecoinformatics.org/knb/metacat";
//  private final int ENTITY_NUMBER_EXPECTED = 1;
  
  //private final String TEST_DOCUMENT = "tao.12103.2";
  private final String TEST_DOCUMENT = "tao.12106.2";
  private final String TEST_SERVER = "http://pacific.msi.ucsb.edu:8080/knb/metacat";
  private final int ENTITY_NUMBER_EXPECTED = 1;
  private final String ENTITY_NAME = "head-line.data";
  private final String TABLE_NAME = "head_line_data";
  private final int NUMBER_OF_COLUMNS = 2;
  private final String COLUMN_1 = "column1";
  private final String COLUMN_2 = "column2";
  
  
  /*
   * Constructors
   */

  /**
   * Because DataManagerTest is a subclass of TestCase, it must provide a
   * constructor with a String parameter.
   * 
   * @param name   the name of a test method to run
   */
  public DataManagerTest(String name) {
    super(name);
  }
  
  
  /*
   * Class methods
   */
  
  /**
   * Create a suite of tests to be run together.
   */
  public static Test suite() {
    TestSuite testSuite = new TestSuite();
    
    testSuite.addTest(new DataManagerTest("initialize"));
    testSuite.addTest(new DataManagerTest("testParseMetadata"));
    testSuite.addTest(new DataManagerTest("testDownloadData"));
    testSuite.addTest(new DataManagerTest("testLoadDataToDB"));
    
    return testSuite;
  }
  
  
  /*
   * Instance methods
   */
  
  /**
   * Run an initial test that always passes to check that the test harness
   * is working.
   */
  public void initialize() {
    assertTrue(1 == 1);
  }
  
  
  /**
   * Establish a testing framework by initializing appropriate objects.
   */
  public void setUp() throws Exception {
    super.setUp();
    DatabaseConnectionPoolInterfaceTest connectionPool = 
                                      new DatabaseConnectionPoolInterfaceTest();
    String dbAdapterName = connectionPool.getDBAdapterName();
    dataManager = DataManager.getInstance(connectionPool, dbAdapterName);
  }
  
  
  /**
   * Release any objects after tests are complete.
   */
  public void tearDown() throws Exception {
    dataManager = null;
    super.tearDown();
  }
  
  
  /**
   * Unit test for the DataManager.downloadData() methods (Use Case #2).
   * 
   * @throws MalformedURLException
   * @throws IOException
   * @throws Exception
   */
  public void testDownloadData() 
          throws MalformedURLException, IOException, Exception {
    DataPackage dataPackage = null;
    String documentURL = TEST_SERVER + 
                         "?action=read&qformat=xml&docid=" + 
                         TEST_DOCUMENT;
    InputStream metadataInputStream;
    boolean success = false;
    DataStorageTest[] testStorageList = new DataStorageTest[1];
    URL url;
    
    try {
      url = new URL(documentURL);
      metadataInputStream = url.openStream();
      dataPackage = dataManager.parseMetadata(metadataInputStream);
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
      throw(e);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw(e);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw(e);
    }
    
    if (dataPackage != null) {
      testStorageList[0] = new DataStorageTest();
      success = dataManager.downloadData(dataPackage, 
                                         endPointInfo,
                                         testStorageList);
    }
    
    /*
     * Assert that dataManager.downloadData() returned a 'true' value,
     * indicating success.
     */
    assertTrue("downloadData() was not successful", success);
  }
 
  
  /**
   * Unit test for the DataManager.loadDataToDB() methods (Use Case #3).
   * This methods also tests two additional methods in the API:
   * 
   *   getDBFieldNames()
   *   getDBTableName()
   * 
   * @throws MalformedURLException
   * @throws IOException
   * @throws Exception
   */
  public void testLoadDataToDB() 
        throws MalformedURLException, IOException, Exception {
    DataPackage dataPackage = null;
    String documentURL = TEST_SERVER + 
                         "?action=read&qformat=xml&docid=" + 
                         TEST_DOCUMENT;
    InputStream metadataInputStream;
    boolean success = false;
    URL url;
    
    try {
      url = new URL(documentURL);
      metadataInputStream = url.openStream();
      dataPackage = dataManager.parseMetadata(metadataInputStream);
      
      if (dataPackage != null) {
        success = dataManager.loadDataToDB(dataPackage, endPointInfo);
        
        /*
         * Test that we can access the correct database table name and field 
         * names for this packageID and entity name.
         */
        if (success) {
          String packageID = TEST_DOCUMENT;
          String entityName = ENTITY_NAME;
          String tableName = dataManager.getDBTableName(packageID, entityName);
          assertNotNull("null value for tableName", tableName);
          assertEquals("tableName does not equal expected value", 
                       tableName, 
                       TABLE_NAME);
          String[] fieldNames = 
                             dataManager.getDBFieldNames(packageID, entityName);         
          assertNotNull("null value for fieldNames array", fieldNames);   
          assertEquals("Incorrect number of columns found", 
                       fieldNames.length, NUMBER_OF_COLUMNS);
          assertEquals("First field name does not equal expected value",
                       fieldNames[0], COLUMN_1);
          assertEquals("Second field name does not equal expected value",
                       fieldNames[1], COLUMN_2);
        }
        
        dataManager.dropTables(dataPackage);  // Clean-up test tables
      }
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
      throw(e);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw(e);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw(e);
    }

    /*
     * Assert that dataManager.loadDataToDB() returned a 'true' value,
     * indicating success.
     */
    assertTrue("loadDataToDB() was not successful", success);
  }

  
  /**
   * Unit test for the DataManager.parseMetadata() method (Use Case #1). 
   * 
   * @throws MalformedURLException
   * @throws IOException
   * @throws Exception
   */
  public void testParseMetadata()
        throws MalformedURLException, IOException, Exception {
    DataPackage dataPackage = null;
    InputStream metadataInputStream;
    String documentURL = TEST_SERVER + 
                         "?action=read&qformat=xml&docid=" + 
                         TEST_DOCUMENT;
    URL url;
    
    try {
      url = new URL(documentURL);
      metadataInputStream = url.openStream();
      dataPackage = dataManager.parseMetadata(metadataInputStream);
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
      throw(e);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw(e);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw(e);
    }

    /*
     * Assert that dataManager.parseMetadata() returned a non-null 
     * dataPackage object.
     */
    assertNotNull("Data package is null", dataPackage);

    /*
     * Compare the number of entities expected in the data package to the
     * number of entities found by the parser.
     */
    if (dataPackage != null) {
      int entityNumberFound = dataPackage.getEntityNumber();
      assertEquals("Number of entites does not match expected value: ",
                   ENTITY_NUMBER_EXPECTED,
                   entityNumberFound);
    }
    
  }

}
