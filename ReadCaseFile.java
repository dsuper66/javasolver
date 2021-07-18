import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReadCaseFile {
    static InputFieldMapping inputFieldMapping = new InputFieldMapping();
    static ModelElementDefService modelElementDefService = new ModelElementDefService();
    static ModelElementDataService modelElementDataService = new ModelElementDataService();

    public static void readCase() throws IOException {

        //DAILY
        //here enode is mapped to pnode
        //Map a field name to an element type
        String sectionName = "PNODE";
        inputFieldMapping.addFieldElementMap(sectionName,"PNODENAME","pnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"KEY1","enode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"KEY2","enode",2);
        inputFieldMapping.addFieldElementMap(sectionName,"KEY3","enode",3);
        //Map a field name to a property type
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"FACTOR","enode","enodePnodeFactor");
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"PNODENAME","enode","enodePnode");

        //MSSNET
        //here the network enode is mapped to enode
        sectionName = "NODE";
        inputFieldMapping.addFieldElementMap(sectionName,"ID_ENODE","nwEnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"ID_ST","enode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"ID_KV","enode",2);
        inputFieldMapping.addFieldElementMap(sectionName,"ID_EQUIPMENT","enode",3);

        inputFieldMapping.addFieldPropertyMap(
                sectionName,"ID_ENODE","enode","nwEnodeEnode");

        //TIME-BASED MSSNET
        //here the network enode is mapped to bus
        sectionName = "ENODEBUS";
        inputFieldMapping.addFieldElementMap(sectionName,"ID_ENODE","nwEnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"ID_BUS","bus",1);

        inputFieldMapping.addFieldPropertyMap(
                sectionName,"ID_BUS","nwEnode","nwEnodeBus");

        //PERIOD
        //BIDSANDOFFERS,1.0,PNODENAME,TRADERID,INTERVAL,TRADETYPE,TRADERBLOCKALTKEY,TRADERBLOCKTRANCHE,
        // TRADERBLOCKLIMIT,TRADERBLOCKPRICE,SIXSEC,RESERVEPERCENT,DISPATCHABLE
        //Map a field name to an element type
        sectionName = "BIDSANDOFFERS";
        inputFieldMapping.addFieldElementMap(
                sectionName,"PNODENAME","pnode",1);
        inputFieldMapping.addFieldElementMap(
                sectionName,"TRADERBLOCKALTKEY","enOfferTranche",1);
        inputFieldMapping.addFieldElementMap(
                sectionName,"TRADERBLOCKTRANCHE","enOfferTranche",2);
        //Map a field name to a property type
        inputFieldMapping.addFieldPropertyMap(sectionName,"TRADERBLOCKLIMIT",
                "enOfferTranche","trancheLimit");
        inputFieldMapping.addFieldPropertyMap(sectionName,"TRADERBLOCKPRICE",
                "enOfferTranche","tranchePrice");
        inputFieldMapping.addFieldPropertyMap(sectionName,"PNODENAME",
                "enOfferTranche","tranchePnode");

        //I,MSSDATA,PNODELOAD,1.0,PNODENAME,INTERVAL,LOADAREAID,ACTUALLOAD,SOURCEOFACTUAL,INSTRUCTEDSHED,
        // CONFORMINGFACTOR,NONCONFORMINGLOAD,CONFORMINGFORECAST,ISNCL,ISBAD,ISOVERRIDE,INSTRUCTEDSHEDACTIVE,DISPATCHEDLOAD,DISPATCHEDGEN

        //I,NETDATA,BRANCHBUS,1.0,ID_BRANCH,ID_FROMBUS,ID_TOBUS,SUSCEPTANCE,RESISTANCE,REMOVE

        //Map an element type to a property type
        //(allows for concatenated element i.d.)
        //inputFieldMapping.addElementPropertyMap("enode","pnodeEnode");

        //Interval
        LocalDateTime caseInterval =
                LocalDateTime.of(2021, 7, 06,10,0);

        //Case file names
        //"/Users/davidbullen/java/MSS_51112021071200687_0X/MSS_51112021071200687_0X.DAILY";
        String caseFileDir = "/Users/davidbullen/java/MSS_51112021071200687_0X/";
        String caseId = "MSS_51112021071200687_0X";

        //MSS_51112021071200687_0X_06-JUL-2021_10_00_0.MSSNET
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("dd-MMM-yyyy_hh_mm", Locale.ENGLISH);
        String timeBasedMSSNET = "_" + caseInterval.format(dateFormatter).toUpperCase() + "_0.MSSNET";

        Set<String> caseTypes = Set.of(".DAILY", ".PERIOD",timeBasedMSSNET);


        //Set the interval for filtering in the file
        //06-JUL-2021 00:00
        dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm", Locale.ENGLISH);
        String caseIntervalInFile = caseInterval.format(dateFormatter).toUpperCase();
        System.out.println(">>>>"+ caseIntervalInFile);

        for (String caseType : caseTypes){
            String fileName = caseFileDir + caseId + caseType;
            BufferedReader bufferedReader =
                    new BufferedReader(new FileReader(fileName));
            System.out.println("file:" + fileName);

            String curLine;
            HashMap<String, Map<Integer, Integer>> elementTypeFieldMaps = new HashMap<>();
            HashMap<String, Integer> propertyTypeFieldMaps = new HashMap<>();

            Boolean dataIsIntervalBased = false; //If interval based then filter data on interval
            while ((curLine = bufferedReader.readLine()) != null) {

                //HEADER: Get the headers and see which match elements and properties
                if (curLine.startsWith("I")) {

                    System.out.println(curLine);
                    List<String> fieldNames = Arrays.asList(curLine.split(","));
                    sectionName = fieldNames.get(2);
                    //Element mapping
                    elementTypeFieldMaps = inputFieldMapping.getElementFieldMapForSectionFieldNames(
                            sectionName, fieldNames);
                    System.out.println("elementTypeFieldMaps:" + elementTypeFieldMaps);
                    //Property mapping
                    propertyTypeFieldMaps = inputFieldMapping.getPropertyFieldMapForSectionFieldNames(fieldNames);
                    System.out.println("propertyTypeFieldMaps:" + propertyTypeFieldMaps);
                    //Interval based data may need to be filtered by interval
                    dataIsIntervalBased = curLine.contains("INTERVAL");
                    System.out.println("dataIsIntervalBased:" + dataIsIntervalBased);
                }

                //DATA: Create the elements and properties
                else if (curLine.startsWith("D") && (!dataIsIntervalBased || curLine.contains(caseIntervalInFile))) {
                    List<String> fieldData = Arrays.asList(curLine.split(","));

                    //Elements
                    //The elementId is a concatenation of the fields for the elementType
                    //For each element type that has a mapping from the header processing
                    //create the elementId
                    HashMap<String, String> elementIdsAndTypeToAdd = new HashMap<>();
                    for (String elementType : elementTypeFieldMaps.keySet()) {
                        Map<Integer, Integer> orderNumFieldNum = elementTypeFieldMaps.get(elementType);
                        String elementId = "";
                        //Create the elementId from one or more fields
                        for (Integer orderNum : orderNumFieldNum.keySet()) {
                            Integer fieldNum = orderNumFieldNum.get(orderNum);
                            elementId += " " + fieldData.get(fieldNum - 1);
                        }
                        elementIdsAndTypeToAdd.put(elementId, elementType);
                    }
                    //System.out.println("elementTypes:" + elementIdAndTypeToAdd);

                    //Add the elements
                    for (var elementIdAndType : elementIdsAndTypeToAdd.entrySet()) {
                        modelElementDataService.addElement(
                                elementIdAndType.getKey(), elementIdAndType.getValue());
                    }

                    //Get and assign the Properties
                    for (String propertyType : propertyTypeFieldMaps.keySet()) {
                        Integer fieldNum = propertyTypeFieldMaps.get(propertyType);
                        String propertyValue = fieldData.get(fieldNum - 1);
                        //System.out.println("propertyType:" + propertyType + " propertyValue:" + propertyValue);

                        //If any of the element types have this property then assign the value
                        for (var elementIdAndType : elementIdsAndTypeToAdd.entrySet()) {
                            Boolean elementHasThisProperty = modelElementDefService.elementTypeHasProperty(
                                            elementIdAndType.getValue(), propertyType);
                            if (elementHasThisProperty) {
                                modelElementDataService.assignPropertyValue(
                                        elementIdAndType.getKey(), propertyType, propertyValue);
                            }
                        }
                    }
                }
            }
            bufferedReader.close();
        }
    }
}
