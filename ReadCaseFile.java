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
        //Need to map pnode (market) to bus (network)

        //DAILY
        //here enode is mapped to pnode
        //Map a field name to an element type
        inputFieldMapping.addFieldElementMap("PNODE","PNODENAME","pnode",1);
        inputFieldMapping.addFieldElementMap("PNODE","KEY1","enode",1);
        inputFieldMapping.addFieldElementMap("PNODE","KEY2","enode",2);
        inputFieldMapping.addFieldElementMap("PNODE","KEY3","enode",3);
        //Map a field name to a property type
        inputFieldMapping.addFieldPropertyMap(
                "PNODE","FACTOR","ENODE","enodePnodeFactor");
        inputFieldMapping.addFieldPropertyMap(
                "PNODE","PNODENAME","ENODE","enodePnode");

        //MSSNET
        //here the network enode is mapped to enode
        inputFieldMapping.addFieldElementMap("NODE","ID_ENODE","nwEnode",1);
        inputFieldMapping.addFieldElementMap("NODE","ID_ST","enode",1);
        inputFieldMapping.addFieldElementMap("NODE","ID_KV","enode",2);
        inputFieldMapping.addFieldElementMap("NODE","ID_EQUIPMENT","enode",3);

        //TIME-BASED MSSNET
        //here the network enode is mapped to bus
        inputFieldMapping.addFieldElementMap(
                "ENODEBUS","ID_ENODE","nwEnode",1);
        inputFieldMapping.addFieldElementMap(
                "ENODEBUS","ID_BUS","bus",1);

        //PERIOD
        //BIDSANDOFFERS,1.0,PNODENAME,TRADERID,INTERVAL,TRADETYPE,TRADERBLOCKALTKEY,TRADERBLOCKTRANCHE,
        // TRADERBLOCKLIMIT,TRADERBLOCKPRICE,SIXSEC,RESERVEPERCENT,DISPATCHABLE
        //Map a field name to an element type
        inputFieldMapping.addFieldElementMap(
                "BIDSANDOFFERS","PNODENAME","pnode",1);
        inputFieldMapping.addFieldElementMap(
                "BIDSANDOFFERS","TRADERBLOCKALTKEY","enOfferTranche",1);
        inputFieldMapping.addFieldElementMap(
                "BIDSANDOFFERS","TRADERBLOCKTRANCHE","enOfferTranche",2);

        //Map a field name to a property type
        inputFieldMapping.addFieldPropertyMap(
                "BIDSANDOFFERS","TRADERBLOCKLIMIT",
                "enOfferTranche","trancheLimit");
        inputFieldMapping.addFieldPropertyMap(
                "BIDSANDOFFERS","TRADERBLOCKPRICE",
                "enOfferTranche","tranchePrice");
        inputFieldMapping.addFieldPropertyMap(
                "BIDSANDOFFERS","PNODENAME",
                "enOfferTranche","tranchePnode");

        //I,MSSDATA,PNODELOAD,1.0,PNODENAME,INTERVAL,LOADAREAID,ACTUALLOAD,SOURCEOFACTUAL,INSTRUCTEDSHED,
        // CONFORMINGFACTOR,NONCONFORMINGLOAD,CONFORMINGFORECAST,ISNCL,ISBAD,ISOVERRIDE,INSTRUCTEDSHEDACTIVE,DISPATCHEDLOAD,DISPATCHEDGEN

        //I,NETDATA,BRANCHBUS,1.0,ID_BRANCH,ID_FROMBUS,ID_TOBUS,SUSCEPTANCE,RESISTANCE,REMOVE

        //Map an element type to a property type
        //(allows for concatenated element i.d.)
        //inputFieldMapping.addElementPropertyMap("enode","pnodeEnode");

        String caseFileDir = "/Users/davidbullen/java/MSS_51112021071200687_0X/";
        String caseId = "MSS_51112021071200687_0X";
        Set<String> caseTypes = Set.of(".DAILY", ".PERIOD");
        //MSS_51112021071200687_0X_06-JUL-2021_10_00_0.MSSNET
        //"/Users/davidbullen/java/MSS_51112021071200687_0X/MSS_51112021071200687_0X.DAILY";

        LocalDateTime caseInterval =
                LocalDateTime.of(2021, 7, 06,10,0);
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm", Locale.ENGLISH);
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
            while ((curLine = bufferedReader.readLine()) != null) {

                //HEADER: Get the headers and see which match elements and properties
                if (curLine.startsWith("I")) {

                    System.out.println(curLine);
                    List<String> fieldNames = Arrays.asList(curLine.split(","));
                    String sectionName = fieldNames.get(2);
                    //Element mapping
                    elementTypeFieldMaps = inputFieldMapping.getElementFieldMapForSectionFieldNames(
                            sectionName, fieldNames);
                    System.out.println("elementTypeFieldMaps:" + elementTypeFieldMaps);
                    //Property mapping
                    propertyTypeFieldMaps = inputFieldMapping.getPropertyFieldMapForSectionFieldNames(fieldNames);
                    System.out.println("propertyTypeFieldMaps:" + propertyTypeFieldMaps);
                }

                //DATA: Create the elements and properties
                else if (curLine.startsWith("D") && curLine.contains(caseIntervalInFile)) {
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
                            Boolean elementHasThisProperty =
                                    modelElementDefService.elementTypeHasProperty(
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
